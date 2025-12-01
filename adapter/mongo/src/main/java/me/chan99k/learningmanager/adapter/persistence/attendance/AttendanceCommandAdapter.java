package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;
import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceReservationDocument;
import me.chan99k.learningmanager.application.attendance.requires.AttendanceCommandRepository;
import me.chan99k.learningmanager.domain.attendance.Attendance;

@Repository
public class AttendanceCommandAdapter implements AttendanceCommandRepository {

	private static final Logger log = LoggerFactory.getLogger(AttendanceCommandAdapter.class);

	private final AttendanceMongoRepository attendanceRepository;
	private final AttendanceReservationMongoRepository reservationRepository;

	public AttendanceCommandAdapter(
		AttendanceMongoRepository attendanceRepository,
		AttendanceReservationMongoRepository reservationRepository
	) {
		this.attendanceRepository = attendanceRepository;
		this.reservationRepository = reservationRepository;
	}

	@Override
	public Attendance save(Attendance attendance) {
		String sessionMemberKey = attendance.getSessionId() + ":" + attendance.getMemberId();

		// 출석에 대한 예약 생성 또는 재사용
		AttendanceReservationDocument reservation = createOrReuseReservation(
			attendance.getSessionId(), attendance.getMemberId(), sessionMemberKey);

		try {
			// 실제 출석 데이터 저장
			AttendanceDocument attDoc = AttendanceDocument.from(attendance);
			AttendanceDocument savedDoc = attendanceRepository.save(attDoc);

			// 예약 커밋 마킹
			reservation.markCommitted();
			reservationRepository.save(reservation);

			// 예약 정리
			try {
				reservationRepository.delete(reservation);
			} catch (Exception e) {
				log.warn("[System] 예약 정리 실패 - TTL로 자동 정리됩니다: operationId={}", reservation.getOperationId(), e);
			}

			return savedDoc.toDomain();

		} catch (Exception e) {
			// 실패 시 예약 롤백
			rollbackReservation(reservation, e);
			throw e;
		}
	}

	private AttendanceReservationDocument createOrReuseReservation(Long sessionId, Long memberId,
		String sessionMemberKey) {
		Optional<AttendanceReservationDocument> existing = reservationRepository
			.findBySessionMemberKey(sessionMemberKey);

		if (existing.isPresent()) {
			AttendanceReservationDocument reservation = existing.get();

			switch (reservation.getStatus()) {
				case FAILED:
					// FAILED 상태면 재사용 가능 검사
					if (reservation.isReusable()) {
						log.info("[System] FAILED 예약 재사용: operationId={}, retryCount={}",
							reservation.getOperationId(), reservation.getRetryCount());

						reservation.resetForReuse();
						return reservationRepository.save(reservation);
					} else {
						throw new RuntimeException("[System] 출석 재시도 한계 초과: " + reservation.getRetryCount());
					}

				case RESERVED:
					// 오래된 RESERVED는 FAILED로 간주하고 재사용
					if (reservation.isStuck()) {
						log.warn("[System] 오래된 RESERVED 예약 발견 - FAILED로 전환: {}",
							reservation.getOperationId());
						reservation.markFailed("Timeout");
						reservation.resetForReuse();
						return reservationRepository.save(reservation);
					} else {
						throw new RuntimeException("[System] 이미 처리 중인 요청입니다");
					}

				case COMMITTED:
					// 이미 성공적으로 처리됨 - 데이터 일관성 확인
					Optional<AttendanceDocument> existingAttendance = attendanceRepository
						.findBySessionIdAndMemberId(sessionId, memberId);
					if (existingAttendance.isPresent()) {
						throw new RuntimeException("[System] 이미 체크인 완료되었습니다");
					} else {
						// 데이터 불일치 - 복구 시도
						log.warn("[System] 데이터 불일치 발견 - 예약은 COMMITTED이나 출석 데이터 없음: operationId={}",
							reservation.getOperationId());
						reservation.markFailed("Data inconsistency");
						reservation.resetForReuse();
						return reservationRepository.save(reservation);
					}

				case COMPLETED:
					// 완료된 예약 삭제 후 새로 생성
					reservationRepository.delete(reservation);
					break;
			}
		}

		// 새 예약 생성
		try {
			AttendanceReservationDocument newReservation = AttendanceReservationDocument.create(sessionId, memberId);
			return reservationRepository.save(newReservation);
		} catch (DuplicateKeyException e) {
			// 동시 요청으로 예약이 이미 생성된 경우 재시도
			log.info("[System] 동시 예약 생성 감지 - 재조회 시도: sessionMemberKey={}", sessionMemberKey);
			return createOrReuseReservation(sessionId, memberId, sessionMemberKey);
		}
	}

	private void rollbackReservation(AttendanceReservationDocument reservation, Exception cause) {
		try {
			reservation.markFailed(cause.getMessage());
			reservationRepository.save(reservation);

			log.error("출석 처리 실패로 예약 FAILED 마킹: operationId={}, error={}",
				reservation.getOperationId(), cause.getMessage());

		} catch (Exception rollbackError) {
			log.error("예약 FAILED 마킹 실패 - TTL이 정리할 예정: operationId={}, rollbackError={}",
				reservation.getOperationId(), rollbackError.getMessage());
		}
	}
}

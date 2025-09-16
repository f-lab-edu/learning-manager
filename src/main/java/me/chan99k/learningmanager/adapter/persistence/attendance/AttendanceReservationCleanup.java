package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceReservationCleanup {

	private static final Logger log = LoggerFactory.getLogger(AttendanceReservationCleanup.class);

	private final AttendanceReservationMongoRepository reservationRepository;

	public AttendanceReservationCleanup(AttendanceReservationMongoRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Scheduled(fixedDelay = 60000) // 1분마다 실행
	public void cleanupStuckReservations() {
		try {
			Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);
			List<AttendanceReservationDocument> stuckReservations = reservationRepository
				.findStuckReservations(cutoff);

			for (AttendanceReservationDocument reservation : stuckReservations) {
				log.warn("[System] 좀비 예약 발견 및 FAILED 전환: operationId={}, sessionMemberKey={}, age={}분",
					reservation.getOperationId(),
					reservation.getSessionMemberKey(),
					ChronoUnit.MINUTES.between(reservation.getReservedAt(), Instant.now()));

				reservation.markFailed("Stuck reservation cleanup");
				reservationRepository.save(reservation);
			}

			// 재시도 한계 초과한 FAILED 예약 삭제
			List<AttendanceReservationDocument> exhaustedReservations = reservationRepository
				.findByStatusAndRetryCountGreaterThan(
					AttendanceReservationDocument.ReservationStatus.FAILED, 3);

			for (AttendanceReservationDocument reservation : exhaustedReservations) {
				log.info("[System] 재시도 한계 초과 예약 삭제: operationId={}, sessionMemberKey={}, retryCount={}",
					reservation.getOperationId(),
					reservation.getSessionMemberKey(),
					reservation.getRetryCount());
				reservationRepository.delete(reservation);
			}

			if (!stuckReservations.isEmpty() || !exhaustedReservations.isEmpty()) {
				log.info("[System] 예약 정리 완료 - 좀비: {}, 한계초과: {}",
					stuckReservations.size(), exhaustedReservations.size());
			}

		} catch (Exception e) {
			log.error("[System] 예약 정리 스케줄러 실패", e);
		}
	}

	@Scheduled(fixedDelay = 300000) // 5분마다 실행
	public void reportReservationHealth() {
		try {
			long reservedCount = reservationRepository.countByStatus(
				AttendanceReservationDocument.ReservationStatus.RESERVED);
			long failedCount = reservationRepository.countByStatus(
				AttendanceReservationDocument.ReservationStatus.FAILED);
			long committedCount = reservationRepository.countByStatus(
				AttendanceReservationDocument.ReservationStatus.COMMITTED);

			if (reservedCount > 0 || failedCount > 0 || committedCount > 0) {
				log.info("[System] 예약 상태 현황 - RESERVED: {}, FAILED: {}, COMMITTED: {}",
					reservedCount, failedCount, committedCount);
			}

			// 경고 임계값 체크
			if (reservedCount > 10) {
				log.warn("RESERVED 예약이 많음: {}개 - 시스템 부하 확인 필요", reservedCount);
			}

			if (failedCount > 20) {
				log.warn("FAILED 예약이 많음: {}개 - 시스템 문제 확인 필요", failedCount);
			}

		} catch (Exception e) {
			log.error("예약 헬스체크 실패", e);
		}
	}
}
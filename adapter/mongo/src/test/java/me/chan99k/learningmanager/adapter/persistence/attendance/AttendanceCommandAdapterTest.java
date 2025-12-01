package me.chan99k.learningmanager.adapter.persistence.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;
import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceReservationDocument;
import me.chan99k.learningmanager.domain.attendance.Attendance;

@ExtendWith(MockitoExtension.class)
class AttendanceCommandAdapterTest {

	private static final Long SESSION_ID = 1L;
	private static final Long MEMBER_ID = 100L;
	private static final String SESSION_MEMBER_KEY = "1:100";

	@Mock
	private AttendanceMongoRepository attendanceRepository;

	@Mock
	private AttendanceReservationMongoRepository reservationRepository;

	@InjectMocks
	private AttendanceCommandAdapter attendanceCommandAdapter;

	@Test
	@DisplayName("[Success] 새로운 출석 저장 - 기존 출석 예약 없음")
	void saveAttendance_NewAttendance_Success() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceDocument attendanceDoc = mock(AttendanceDocument.class);
		Attendance returnedAttendance = mock(Attendance.class);
		when(attendanceDoc.toDomain()).thenReturn(returnedAttendance);

		AttendanceReservationDocument reservation = mock(AttendanceReservationDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(Optional.empty());
		when(reservationRepository.save(any(AttendanceReservationDocument.class))).thenReturn(reservation);
		when(attendanceRepository.save(any(AttendanceDocument.class))).thenReturn(attendanceDoc);

		// When
		Attendance result = attendanceCommandAdapter.save(attendance);

		// Then
		assertThat(result).isNotNull();
		verify(reservationRepository).findBySessionMemberKey(SESSION_MEMBER_KEY);
		verify(reservationRepository, times(2)).save(any(AttendanceReservationDocument.class)); // 생성 + 커밋
		verify(attendanceRepository).save(any(AttendanceDocument.class));
		verify(reservation).markCommitted();
		verify(reservationRepository).delete(reservation);
	}

	@Test
	@DisplayName("[Success] FAILED 예약 재사용하여 출석 저장")
	void saveAttendance_ReuseFailedReservation_Success() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceDocument attendanceDoc = mock(AttendanceDocument.class);
		Attendance returnedAttendance = mock(Attendance.class);
		when(attendanceDoc.toDomain()).thenReturn(returnedAttendance);

		AttendanceReservationDocument failedReservation = mock(AttendanceReservationDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(
			Optional.of(failedReservation));
		when(failedReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.FAILED);
		when(failedReservation.isReusable()).thenReturn(true);
		when(reservationRepository.save(failedReservation)).thenReturn(failedReservation);
		when(attendanceRepository.save(any(AttendanceDocument.class))).thenReturn(attendanceDoc);

		// When
		Attendance result = attendanceCommandAdapter.save(attendance);

		// Then
		assertThat(result).isNotNull();
		verify(failedReservation).resetForReuse();
		verify(failedReservation).markCommitted();
		verify(reservationRepository).delete(failedReservation);
	}

	@Test
	@DisplayName("[Failure] FAILED 예약이지만 재시도 한계 초과")
	void saveAttendance_FailedReservationExceedsRetryLimit_ThrowsException() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceReservationDocument failedReservation = mock(AttendanceReservationDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(
			Optional.of(failedReservation));
		when(failedReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.FAILED);
		when(failedReservation.isReusable()).thenReturn(false);
		when(failedReservation.getRetryCount()).thenReturn(3);

		// When & Then
		assertThatThrownBy(() -> attendanceCommandAdapter.save(attendance))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("재시도 한계 초과: 3");
	}

	@Test
	@DisplayName("[Success] 오래된 RESERVED 예약을 FAILED로 전환 후 재사용")
	void saveAttendance_StuckReservationConverted_Success() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceDocument attendanceDoc = mock(AttendanceDocument.class);
		Attendance returnedAttendance = mock(Attendance.class);
		when(attendanceDoc.toDomain()).thenReturn(returnedAttendance);

		AttendanceReservationDocument stuckReservation = mock(AttendanceReservationDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(
			Optional.of(stuckReservation));
		when(stuckReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.RESERVED);
		when(stuckReservation.isStuck()).thenReturn(true);
		when(reservationRepository.save(stuckReservation)).thenReturn(stuckReservation);
		when(attendanceRepository.save(any(AttendanceDocument.class))).thenReturn(attendanceDoc);

		// When
		Attendance result = attendanceCommandAdapter.save(attendance);

		// Then
		assertThat(result).isNotNull();
		verify(stuckReservation).markFailed("Timeout");
		verify(stuckReservation).resetForReuse();
		verify(stuckReservation).markCommitted();
	}

	@Test
	@DisplayName("[Failure] 현재 처리 중인 예약이 있음")
	void saveAttendance_ReservationInProgress_ThrowsException() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceReservationDocument activeReservation = mock(AttendanceReservationDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(
			Optional.of(activeReservation));
		when(activeReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.RESERVED);
		when(activeReservation.isStuck()).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> attendanceCommandAdapter.save(attendance))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("[System] 이미 처리 중인 요청입니다");
	}

	@Test
	@DisplayName("[Success] COMMITTED 상태이지만 데이터 불일치 - 복구")
	void saveAttendance_CommittedButDataInconsistency_Recovery() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceDocument attendanceDoc = mock(AttendanceDocument.class);
		Attendance returnedAttendance = mock(Attendance.class);
		when(attendanceDoc.toDomain()).thenReturn(returnedAttendance);

		AttendanceReservationDocument committedReservation = mock(AttendanceReservationDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(
			Optional.of(committedReservation));
		when(committedReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.COMMITTED);
		when(attendanceRepository.findBySessionIdAndMemberId(SESSION_ID, MEMBER_ID)).thenReturn(Optional.empty());
		when(reservationRepository.save(committedReservation)).thenReturn(committedReservation);
		when(attendanceRepository.save(any(AttendanceDocument.class))).thenReturn(attendanceDoc);

		// When
		Attendance result = attendanceCommandAdapter.save(attendance);

		// Then
		assertThat(result).isNotNull();
		verify(committedReservation).markFailed("Data inconsistency");
		verify(committedReservation).resetForReuse();
	}

	@Test
	@DisplayName("[Failure] 이미 체크인 완료된 상태")
	void saveAttendance_AlreadyCheckedIn_ThrowsException() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceReservationDocument committedReservation = mock(AttendanceReservationDocument.class);
		AttendanceDocument existingAttendance = mock(AttendanceDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(
			Optional.of(committedReservation));
		when(committedReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.COMMITTED);
		when(attendanceRepository.findBySessionIdAndMemberId(SESSION_ID, MEMBER_ID)).thenReturn(
			Optional.of(existingAttendance));

		// When & Then
		assertThatThrownBy(() -> attendanceCommandAdapter.save(attendance))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("[System] 이미 체크인 완료되었습니다");
	}

	@Test
	@DisplayName("[Success] COMPLETED 예약 삭제 후 새 예약 생성")
	void saveAttendance_CompletedReservationDeleted_Success() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceDocument attendanceDoc = mock(AttendanceDocument.class);
		Attendance returnedAttendance = mock(Attendance.class);
		when(attendanceDoc.toDomain()).thenReturn(returnedAttendance);

		AttendanceReservationDocument completedReservation = mock(AttendanceReservationDocument.class);
		AttendanceReservationDocument newReservation = mock(AttendanceReservationDocument.class);

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(
			Optional.of(completedReservation));
		when(completedReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.COMPLETED);
		when(reservationRepository.save(any(AttendanceReservationDocument.class))).thenReturn(newReservation);
		when(attendanceRepository.save(any(AttendanceDocument.class))).thenReturn(attendanceDoc);

		// When
		Attendance result = attendanceCommandAdapter.save(attendance);

		// Then
		assertThat(result).isNotNull();
		verify(reservationRepository).delete(completedReservation);
		verify(newReservation).markCommitted();
	}

	@Test
	@DisplayName("[Failure] 출석 저장 실패 시 예약 롤백")
	void saveAttendance_AttendanceSaveFails_ReservationRollback() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceReservationDocument reservation = mock(AttendanceReservationDocument.class);
		RuntimeException saveException = new RuntimeException("Database error");

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(Optional.empty());
		when(reservationRepository.save(any(AttendanceReservationDocument.class))).thenReturn(reservation);
		when(attendanceRepository.save(any(AttendanceDocument.class))).thenThrow(saveException);

		// When & Then
		assertThatThrownBy(() -> attendanceCommandAdapter.save(attendance))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Database error");

		verify(reservation).markFailed("Database error");
		verify(reservationRepository, atLeast(2)).save(any(AttendanceReservationDocument.class)); // 생성 + 커밋 시도 + 롤백
	}

	@Test
	@DisplayName("[Success] 동시 예약 생성으로 DuplicateKeyException 발생 시 재조회")
	void saveAttendance_DuplicateKeyExceptionDuringReservationCreation_Success() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceDocument attendanceDoc = mock(AttendanceDocument.class);
		Attendance returnedAttendance = mock(Attendance.class);
		when(attendanceDoc.toDomain()).thenReturn(returnedAttendance);

		AttendanceReservationDocument existingReservation = mock(AttendanceReservationDocument.class);
		when(existingReservation.getStatus()).thenReturn(AttendanceReservationDocument.ReservationStatus.FAILED);
		when(existingReservation.isReusable()).thenReturn(true);
		when(existingReservation.getOperationId()).thenReturn("operation-456");
		when(existingReservation.getRetryCount()).thenReturn(1);

		// 첫 번째 호출에서는 빈 Optional 반환 (새 예약 생성 시도)
		// 두 번째 호출에서는 기존 예약 반환 (재조회)
		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(existingReservation));

		// 새 예약 생성 시도에서 DuplicateKeyException 발생
		when(reservationRepository.save(any(AttendanceReservationDocument.class)))
			.thenThrow(new DuplicateKeyException("Duplicate key"))
			.thenReturn(existingReservation);

		when(attendanceRepository.save(any(AttendanceDocument.class))).thenReturn(attendanceDoc);

		// When
		Attendance result = attendanceCommandAdapter.save(attendance);

		// Then
		assertThat(result).isNotNull();
		verify(reservationRepository, times(2)).findBySessionMemberKey(SESSION_MEMBER_KEY);
		verify(existingReservation).resetForReuse();
		verify(existingReservation).markCommitted();
	}

	@Test
	@DisplayName("[Success] 예약 정리 실패 시 로깅만 수행하고 계속 진행")
	void saveAttendance_ReservationCleanupFails_LogsAndContinues() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceDocument attendanceDoc = mock(AttendanceDocument.class);
		Attendance returnedAttendance = mock(Attendance.class);
		when(attendanceDoc.toDomain()).thenReturn(returnedAttendance);

		AttendanceReservationDocument reservation = mock(AttendanceReservationDocument.class);
		when(reservation.getOperationId()).thenReturn("operation-123");

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(Optional.empty());
		when(reservationRepository.save(any(AttendanceReservationDocument.class))).thenReturn(reservation);
		when(attendanceRepository.save(any(AttendanceDocument.class))).thenReturn(attendanceDoc);

		// 예약 삭제 시 예외 발생
		doThrow(new RuntimeException("Delete failed")).when(reservationRepository).delete(reservation);

		// When
		Attendance result = attendanceCommandAdapter.save(attendance);

		// Then
		assertThat(result).isNotNull();
		verify(reservation).markCommitted();
		verify(reservationRepository).delete(reservation);
		// 예외가 발생해도 메소드는 정상적으로 완료되어야 함
	}

	@Test
	@DisplayName("[Failure] 예약 롤백 중 FAILED 마킹도 실패하는 경우")
	void saveAttendance_ReservationRollbackAlsoFails_LogsError() {
		// Given
		Attendance attendance = mock(Attendance.class);
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);

		AttendanceReservationDocument reservation = mock(AttendanceReservationDocument.class);
		when(reservation.getOperationId()).thenReturn("operation-123");

		RuntimeException originalException = new RuntimeException("Original error");
		RuntimeException rollbackException = new RuntimeException("Rollback error");

		when(reservationRepository.findBySessionMemberKey(SESSION_MEMBER_KEY)).thenReturn(Optional.empty());
		when(reservationRepository.save(any(AttendanceReservationDocument.class)))
			.thenReturn(reservation)  // 첫 번째 저장 성공
			.thenThrow(rollbackException);  // 롤백 시 저장 실패

		when(attendanceRepository.save(any(AttendanceDocument.class))).thenThrow(originalException);

		// When & Then
		assertThatThrownBy(() -> attendanceCommandAdapter.save(attendance))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Original error");

		verify(reservation).markFailed("Original error");
		verify(reservationRepository, times(2)).save(any(AttendanceReservationDocument.class));
	}

}
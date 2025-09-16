package me.chan99k.learningmanager.adapter.persistence.attendance;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceReservationCleanupTest {

	@Mock
	private AttendanceReservationMongoRepository reservationRepository;

	@InjectMocks
	private AttendanceReservationCleanup attendanceReservationCleanup;

	@Test
	@DisplayName("[Success] 좀비 예약 정리 - 예약이 있는 경우")
	void cleanupStuckReservations_WithStuckReservations_Success() {
		// Given
		AttendanceReservationDocument stuckReservation = mock(AttendanceReservationDocument.class);
		when(stuckReservation.getOperationId()).thenReturn("operation-123");
		when(stuckReservation.getSessionMemberKey()).thenReturn("1:100");
		when(stuckReservation.getReservedAt()).thenReturn(Instant.parse("2024-01-01T09:00:00Z"));

		List<AttendanceReservationDocument> stuckReservations = List.of(stuckReservation);

		when(reservationRepository.findStuckReservations(any(Instant.class))).thenReturn(stuckReservations);
		when(reservationRepository.findByStatusAndRetryCountGreaterThan(
			AttendanceReservationDocument.ReservationStatus.FAILED, 3))
			.thenReturn(Collections.emptyList());

		// When
		attendanceReservationCleanup.cleanupStuckReservations();

		// Then
		verify(reservationRepository).findStuckReservations(any(Instant.class));
		verify(stuckReservation).markFailed("Stuck reservation cleanup");
		verify(reservationRepository).save(stuckReservation);
	}

	@Test
	@DisplayName("[Success] 재시도 한계 초과 예약 삭제")
	void cleanupStuckReservations_WithExhaustedReservations_Success() {
		// Given
		AttendanceReservationDocument exhaustedReservation = mock(AttendanceReservationDocument.class);
		when(exhaustedReservation.getOperationId()).thenReturn("operation-456");
		when(exhaustedReservation.getSessionMemberKey()).thenReturn("2:200");
		when(exhaustedReservation.getRetryCount()).thenReturn(2);

		List<AttendanceReservationDocument> exhaustedReservations = List.of(exhaustedReservation);

		when(reservationRepository.findStuckReservations(any(Instant.class))).thenReturn(Collections.emptyList());
		when(reservationRepository.findByStatusAndRetryCountGreaterThan(
			AttendanceReservationDocument.ReservationStatus.FAILED, 3))
			.thenReturn(exhaustedReservations);

		// When
		attendanceReservationCleanup.cleanupStuckReservations();

		// Then
		verify(reservationRepository).findByStatusAndRetryCountGreaterThan(
			AttendanceReservationDocument.ReservationStatus.FAILED, 3);
		verify(reservationRepository).delete(exhaustedReservation);
	}

	@Test
	@DisplayName("[Success] 정리할 예약이 없는 경우")
	void cleanupStuckReservations_NoReservationsToCleanup_Success() {
		// Given
		when(reservationRepository.findStuckReservations(any(Instant.class))).thenReturn(Collections.emptyList());
		when(reservationRepository.findByStatusAndRetryCountGreaterThan(
			AttendanceReservationDocument.ReservationStatus.FAILED, 3))
			.thenReturn(Collections.emptyList());

		// When
		attendanceReservationCleanup.cleanupStuckReservations();

		// Then
		verify(reservationRepository).findStuckReservations(any(Instant.class));
		verify(reservationRepository).findByStatusAndRetryCountGreaterThan(
			AttendanceReservationDocument.ReservationStatus.FAILED, 3);
		verify(reservationRepository, never()).save(any());
		verify(reservationRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Failure] 정리 중 예외 발생 - 예외 처리됨")
	void cleanupStuckReservations_ExceptionDuringCleanup_ExceptionHandled() {
		// Given
		when(reservationRepository.findStuckReservations(any(Instant.class)))
			.thenThrow(new RuntimeException("Database connection failed"));

		// When & Then (예외가 전파되지 않음을 확인)
		attendanceReservationCleanup.cleanupStuckReservations();

		verify(reservationRepository).findStuckReservations(any(Instant.class));
	}

	@Test
	@DisplayName("[Success] 예약 상태 헬스체크 - 정상 상황")
	void reportReservationHealth_NormalSituation_Success() {
		// Given
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.RESERVED))
			.thenReturn(3L);
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.FAILED))
			.thenReturn(1L);
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.COMMITTED))
			.thenReturn(0L);

		// When
		attendanceReservationCleanup.reportReservationHealth();

		// Then
		verify(reservationRepository).countByStatus(AttendanceReservationDocument.ReservationStatus.RESERVED);
		verify(reservationRepository).countByStatus(AttendanceReservationDocument.ReservationStatus.FAILED);
		verify(reservationRepository).countByStatus(AttendanceReservationDocument.ReservationStatus.COMMITTED);
	}

	@Test
	@DisplayName("[Warning] 과도한 RESERVED 예약 감지")
	void reportReservationHealth_ExcessiveReserved_WarningLogged() {
		// Given
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.RESERVED))
			.thenReturn(15L); // 임계값(10) 초과
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.FAILED))
			.thenReturn(5L);
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.COMMITTED))
			.thenReturn(2L);

		// When
		attendanceReservationCleanup.reportReservationHealth();

		// Then
		verify(reservationRepository, times(3)).countByStatus(any());
	}

	@Test
	@DisplayName("[Warning] 과도한 FAILED 예약 감지")
	void reportReservationHealth_ExcessiveFailed_WarningLogged() {
		// Given
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.RESERVED))
			.thenReturn(5L);
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.FAILED))
			.thenReturn(25L); // 임계값(20) 초과
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.COMMITTED))
			.thenReturn(1L);

		// When
		attendanceReservationCleanup.reportReservationHealth();

		// Then
		verify(reservationRepository, times(3)).countByStatus(any());
	}

	@Test
	@DisplayName("[Success] 모든 카운트가 0인 경우")
	void reportReservationHealth_AllCountsZero_NoLogging() {
		// Given
		when(reservationRepository.countByStatus(any())).thenReturn(0L);

		// When
		attendanceReservationCleanup.reportReservationHealth();

		// Then
		verify(reservationRepository, times(3)).countByStatus(any());
	}

	@Test
	@DisplayName("[Failure] 헬스체크 중 예외 발생 - 예외 처리됨")
	void reportReservationHealth_ExceptionDuringHealthCheck_ExceptionHandled() {
		// Given
		when(reservationRepository.countByStatus(AttendanceReservationDocument.ReservationStatus.RESERVED))
			.thenThrow(new RuntimeException("Database error"));

		// When & Then (예외가 전파되지 않음을 확인)
		attendanceReservationCleanup.reportReservationHealth();

		verify(reservationRepository).countByStatus(AttendanceReservationDocument.ReservationStatus.RESERVED);
	}

}
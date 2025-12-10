package me.chan99k.learningmanager.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class AttendanceCorrectionApprovalServiceTest {

	private static final String ATTENDANCE_ID = "attendance-123";
	private static final Long SESSION_ID = 1L;
	private static final Long MEMBER_ID = 100L;
	private static final Long APPROVER_ID = 300L;
	private static final Instant FIXED_TIME = Instant.parse("2024-01-01T10:00:00Z");

	@Mock
	private AttendanceQueryRepository attendanceQueryRepository;
	@Mock
	private AttendanceCommandRepository attendanceCommandRepository;
	@Mock
	private Clock clock;
	@InjectMocks
	private AttendanceCorrectionApprovalService service;

	@Test
	@DisplayName("[Success] 출석 수정 요청을 승인한다")
	void approve_success() {
		Attendance attendance = createAttendanceWithPendingRequest();
		when(attendanceQueryRepository.findById(ATTENDANCE_ID)).thenReturn(Optional.of(attendance));
		when(clock.instant()).thenReturn(FIXED_TIME);
		when(attendanceCommandRepository.save(any(Attendance.class))).thenReturn(attendance);

		AttendanceCorrectionApproval.Request request = new AttendanceCorrectionApproval.Request(ATTENDANCE_ID);

		AttendanceCorrectionApproval.Response response = service.approve(APPROVER_ID, request);

		assertThat(response.attendanceId()).isEqualTo(ATTENDANCE_ID);
		assertThat(response.previousStatus()).isEqualTo(AttendanceStatus.PRESENT);
		assertThat(response.newStatus()).isEqualTo(AttendanceStatus.LATE);
		assertThat(response.approvedBy()).isEqualTo(APPROVER_ID);
		verify(attendanceCommandRepository).save(any(Attendance.class));
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 출석 기록 승인 시 예외가 발생한다")
	void approve_fail_if_attendance_not_found() {
		when(attendanceQueryRepository.findById(ATTENDANCE_ID)).thenReturn(Optional.empty());

		AttendanceCorrectionApproval.Request request = new AttendanceCorrectionApproval.Request(ATTENDANCE_ID);

		assertThatThrownBy(() -> service.approve(APPROVER_ID, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", AttendanceProblemCode.ATTENDANCE_NOT_FOUND);
	}

	private Attendance createAttendanceWithPendingRequest() {
		Attendance attendance = Attendance.create(SESSION_ID, MEMBER_ID);
		attendance.setId(ATTENDANCE_ID);
		attendance.checkIn(Clock.systemUTC());
		attendance.requestCorrection(AttendanceStatus.LATE, "지각 처리", 200L, Clock.systemUTC());
		return attendance;
	}
}

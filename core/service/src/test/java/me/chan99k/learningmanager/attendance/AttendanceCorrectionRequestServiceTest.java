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
class AttendanceCorrectionRequestServiceTest {

	private static final String ATTENDANCE_ID = "attendance-123";
	private static final Long SESSION_ID = 1L;
	private static final Long MEMBER_ID = 100L;
	private static final Long REQUESTER_ID = 200L;
	private static final Instant FIXED_TIME = Instant.parse("2024-01-01T10:00:00Z");

	@Mock
	private AttendanceQueryRepository attendanceQueryRepository;
	@Mock
	private AttendanceCommandRepository attendanceCommandRepository;
	@Mock
	private Clock clock;
	@InjectMocks
	private AttendanceCorrectionRequestService service;

	@Test
	@DisplayName("[Success] 출석 수정 요청을 성공적으로 생성한다")
	void request_success() {
		Attendance attendance = createAttendanceWithCheckIn();
		when(attendanceQueryRepository.findById(ATTENDANCE_ID)).thenReturn(Optional.of(attendance));
		when(clock.instant()).thenReturn(FIXED_TIME);
		when(attendanceCommandRepository.save(any(Attendance.class))).thenReturn(attendance);

		AttendanceCorrectionRequest.Request request = new AttendanceCorrectionRequest.Request(
			ATTENDANCE_ID, AttendanceStatus.LATE, "지각 처리 요청"
		);

		AttendanceCorrectionRequest.Response response = service.request(REQUESTER_ID, request);

		assertThat(response.attendanceId()).isEqualTo(ATTENDANCE_ID);
		assertThat(response.currentStatus()).isEqualTo(AttendanceStatus.PRESENT);
		assertThat(response.requestedStatus()).isEqualTo(AttendanceStatus.LATE);
		assertThat(response.reason()).isEqualTo("지각 처리 요청");
		verify(attendanceCommandRepository).save(any(Attendance.class));
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 출석 기록에 요청하면 예외가 발생한다")
	void request_fail_if_attendance_not_found() {
		when(attendanceQueryRepository.findById(ATTENDANCE_ID)).thenReturn(Optional.empty());

		AttendanceCorrectionRequest.Request request = new AttendanceCorrectionRequest.Request(
			ATTENDANCE_ID, AttendanceStatus.LATE, "요청 사유"
		);

		assertThatThrownBy(() -> service.request(REQUESTER_ID, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", AttendanceProblemCode.ATTENDANCE_NOT_FOUND);
	}

	private Attendance createAttendanceWithCheckIn() {
		Attendance attendance = Attendance.create(SESSION_ID, MEMBER_ID);
		attendance.setId(ATTENDANCE_ID);
		attendance.checkIn(Clock.systemUTC());
		return attendance;
	}
}

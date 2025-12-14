package me.chan99k.learningmanager.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionParticipant;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceCheckInServiceTest {

	private static final Long SESSION_ID = 1L;
	private static final Long MEMBER_ID = 100L;
	private static final Long NON_PARTICIPANT_ID = 200L;
	private static final Instant FIXED_TIME = Instant.parse("2024-01-01T10:00:00Z");
	@Mock
	private AttendanceQueryRepository attendanceQueryRepository;
	@Mock
	private AttendanceCommandRepository attendanceCommandRepository;
	@Mock
	private SessionQueryRepository sessionQueryRepository;
	@Mock
	private Clock clock;
	@InjectMocks
	private AttendanceCheckInService attendanceCheckInService;

	@Test
	@DisplayName("[Success] 정상적인 체크인 - 첫 출석")
	void test01() {
		// Given
		Session session = createMockSession();
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		when(attendanceQueryRepository.findBySessionIdAndMemberId(SESSION_ID, MEMBER_ID))
			.thenReturn(Optional.empty());
		when(clock.instant()).thenReturn(FIXED_TIME);

		Attendance savedAttendance = createMockAttendance();
		when(attendanceCommandRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

		AttendanceCheckIn.Request request = new AttendanceCheckIn.Request(SESSION_ID);

		// When
		AttendanceCheckIn.Response response = attendanceCheckInService.checkIn(MEMBER_ID, request);

		// Then
		assertThat(response.attendanceId()).isEqualTo("attendance-id");
		assertThat(response.sessionId()).isEqualTo(SESSION_ID);
		assertThat(response.memberId()).isEqualTo(MEMBER_ID);
		assertThat(response.status()).isEqualTo("PRESENT");

		verify(attendanceCommandRepository).save(any(Attendance.class));
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션에 체크인 시도")
	void test03() {
		// Given
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
		AttendanceCheckIn.Request request = new AttendanceCheckIn.Request(SESSION_ID);

		// When & Then
		assertThatThrownBy(() -> attendanceCheckInService.checkIn(MEMBER_ID, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);
	}

	@Test
	@DisplayName("[Failure] 세션 참여자가 아닌 사용자의 체크인 시도")
	void test04() {
		// Given
		Session session = createMockSession();
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		AttendanceCheckIn.Request request = new AttendanceCheckIn.Request(SESSION_ID);

		// When & Then
		assertThatThrownBy(() -> attendanceCheckInService.checkIn(NON_PARTICIPANT_ID, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.NOT_SESSION_PARTICIPANT);
	}

	private Session createMockSession() {
		Session session = mock(Session.class);
		SessionParticipant participant = mock(SessionParticipant.class);
		when(participant.getMemberId()).thenReturn(MEMBER_ID);
		when(session.getParticipants()).thenReturn(List.of(participant));
		return session;
	}

	private Attendance createMockAttendance() {
		Attendance attendance = mock(Attendance.class);
		when(attendance.getId()).thenReturn("attendance-id");
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
		when(attendance.getMemberId()).thenReturn(MEMBER_ID);
		when(attendance.getFinalStatus()).thenReturn(AttendanceStatus.PRESENT);
		when(attendance.getEvents()).thenReturn(List.of());
		return attendance;
	}
}

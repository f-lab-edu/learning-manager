package me.chan99k.learningmanager.application.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionCommandRepository;
import me.chan99k.learningmanager.session.SessionParticipant;
import me.chan99k.learningmanager.session.SessionParticipantManagement.AddParticipantRequest;
import me.chan99k.learningmanager.session.SessionParticipantManagement.ChangeParticipantRoleRequest;
import me.chan99k.learningmanager.session.SessionParticipantManagement.LeaveSessionRequest;
import me.chan99k.learningmanager.session.SessionParticipantManagement.RemoveParticipantRequest;
import me.chan99k.learningmanager.session.SessionParticipantRole;
import me.chan99k.learningmanager.session.SessionParticipantService;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@ExtendWith(MockitoExtension.class)
class SessionParticipantServiceTest {

	private final Long sessionId = 1L;
	private final Long courseId = 100L;
	private final Long managerId = 200L;
	private final Long hostId = 300L;
	private final Long memberId = 400L;
	@Mock
	private Clock clock;
	@Mock
	private SessionQueryRepository sessionQueryRepository;
	@Mock
	private SessionCommandRepository sessionCommandRepository;
	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private Session session;
	@Mock
	private Course course;
	@InjectMocks
	private SessionParticipantService sessionParticipantService;

	@Test
	@DisplayName("Course Manager는 참여자를 추가할 수 있다")
	void addParticipant_AsCourseManager_Success() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(courseQueryRepository.findManagedCourseById(courseId, managerId))
			.thenReturn(Optional.of(course));
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		// when
		var response = sessionParticipantService.addParticipant(managerId, sessionId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).addParticipant(memberId, SessionParticipantRole.ATTENDEE);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("Session Host는 참여자를 추가할 수 있다")
	void addParticipant_AsSessionHost_Success() {
		// given
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(courseQueryRepository.findManagedCourseById(courseId, hostId))
			.thenReturn(Optional.empty());
		when(session.getParticipants()).thenReturn(List.of(hostParticipant));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new AddParticipantRequest(memberId, SessionParticipantRole.SPEAKER);

		// when
		var response = sessionParticipantService.addParticipant(hostId, sessionId, request);

		// then
		assertThat(response).isNotNull();
		verify(session).addParticipant(memberId, SessionParticipantRole.SPEAKER);
	}

	@Test
	@DisplayName("권한이 없는 사용자는 참여자를 추가할 수 없다")
	void addParticipant_NoPermission_ThrowsDomainException() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(courseQueryRepository.findManagedCourseById(courseId, memberId))
			.thenReturn(Optional.empty());
		when(session.getParticipants()).thenReturn(List.of());

		var request = new AddParticipantRequest(999L, SessionParticipantRole.ATTENDEE);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.addParticipant(memberId, sessionId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.NOT_SESSION_HOST);
	}

	@Test
	@DisplayName("존재하지 않는 세션의 참여자를 추가할 수 없다")
	void addParticipant_SessionNotFound_ThrowsDomainException() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.addParticipant(managerId, sessionId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);
	}

	@Test
	@DisplayName("Course Manager는 참여자를 제거할 수 있다")
	void removeParticipant_AsCourseManager_Success() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(courseQueryRepository.findManagedCourseById(courseId, managerId))
			.thenReturn(Optional.of(course));
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new RemoveParticipantRequest(sessionId, memberId);

		// when
		var response = sessionParticipantService.removeParticipant(managerId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).removeParticipant(memberId);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("Session Host는 참여자를 제거할 수 있다")
	void removeParticipant_AsSessionHost_Success() {
		// given
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of(hostParticipant));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new RemoveParticipantRequest(sessionId, memberId);

		// when
		var response = sessionParticipantService.removeParticipant(hostId, request);

		// then
		assertThat(response).isNotNull();
		verify(session).removeParticipant(memberId);
	}

	@Test
	@DisplayName("권한이 없는 사용자는 참여자를 제거할 수 없다")
	void removeParticipant_NoPermission_ThrowsDomainException() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(courseQueryRepository.findManagedCourseById(courseId, memberId))
			.thenReturn(Optional.empty());
		when(session.getParticipants()).thenReturn(List.of());

		var request = new RemoveParticipantRequest(sessionId, 999L);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.removeParticipant(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.NOT_SESSION_HOST);
	}

	@Test
	@DisplayName("Course Manager는 참여자 역할을 변경할 수 있다")
	void changeParticipantRole_AsCourseManager_Success() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(courseQueryRepository.findManagedCourseById(courseId, managerId))
			.thenReturn(Optional.of(course));
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.SPEAKER);

		// when
		var response = sessionParticipantService.changeParticipantRole(managerId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).changeParticipantRole(memberId, SessionParticipantRole.SPEAKER, clock);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("Session Host는 참여자 역할을 변경할 수 있다")
	void changeParticipantRole_AsSessionHost_Success() {
		// given
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of(hostParticipant));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.ATTENDEE);

		// when
		var response = sessionParticipantService.changeParticipantRole(hostId, request);

		// then
		assertThat(response).isNotNull();
		verify(session).changeParticipantRole(memberId, SessionParticipantRole.ATTENDEE, clock);
	}

	@Test
	@DisplayName("권한이 없는 사용자는 참여자 역할을 변경할 수 없다")
	void changeParticipantRole_NoPermission_ThrowsDomainException() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(courseQueryRepository.findManagedCourseById(courseId, memberId))
			.thenReturn(Optional.empty());
		when(session.getParticipants()).thenReturn(List.of());

		var request = new ChangeParticipantRoleRequest(sessionId, 999L, SessionParticipantRole.SPEAKER);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.changeParticipantRole(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.NOT_SESSION_HOST);
	}

	@Test
	@DisplayName("단독 세션(Course 없음)의 경우 Host만 참여자 관리가 가능하다")
	void addParticipant_StandaloneSession_OnlyHostCanManage() {
		when(session.getCourseId()).thenReturn(null);

		// given
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getParticipants()).thenReturn(List.of(hostParticipant));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		// when
		var response = sessionParticipantService.addParticipant(hostId, sessionId, request);

		// then
		assertThat(response).isNotNull();
		verify(session).addParticipant(memberId, SessionParticipantRole.ATTENDEE);
		verifyNoInteractions(courseQueryRepository);
	}

	@Test
	@DisplayName("단독 세션에서 Host가 아닌 사용자는 참여자 관리를 할 수 없다")
	void addParticipant_StandaloneSession_NonHostCannotManage() {
		when(session.getCourseId()).thenReturn(null);

		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getParticipants()).thenReturn(List.of());

		var request = new AddParticipantRequest(999L, SessionParticipantRole.ATTENDEE);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.addParticipant(memberId, sessionId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.NOT_SESSION_HOST);

		verifyNoInteractions(courseQueryRepository);
	}

	@Test
	@DisplayName("HOST 자신을 제거할 때 다른 HOST가 있으면 성공한다")
	void removeParticipant_HostSelfRemoval_SuccessWithOtherHost() {
		// given
		var hostParticipant1 = mock(SessionParticipant.class);
		when(hostParticipant1.getMemberId()).thenReturn(hostId);
		when(hostParticipant1.getRole()).thenReturn(SessionParticipantRole.HOST);

		var hostParticipant2 = mock(SessionParticipant.class);
		when(hostParticipant2.getMemberId()).thenReturn(999L);
		when(hostParticipant2.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of(hostParticipant1, hostParticipant2));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new RemoveParticipantRequest(sessionId, hostId);

		var response = sessionParticipantService.removeParticipant(hostId, request);

		assertThat(response).isNotNull();
		verify(session).removeParticipant(hostId);
	}

	@Test
	@DisplayName("HOST 자신을 제거할 때 다른 HOST가 없으면 실패한다")
	void removeParticipant_HostSelfRemoval_FailWithoutOtherHost() {
		// given
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		var attendeeParticipant = mock(SessionParticipant.class);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(session.getParticipants()).thenReturn(List.of(hostParticipant, attendeeParticipant));

		var request = new RemoveParticipantRequest(sessionId, hostId);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.removeParticipant(hostId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.HOST_CANNOT_LEAVE_ALONE);

		verify(session, never()).removeParticipant(hostId);
	}

	@Test
	@DisplayName("HOST가 아닌 사용자를 제거하는 것은 자유롭게 가능하다")
	void removeParticipant_NonHostRemoval_Success() {
		// given
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of(hostParticipant));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new RemoveParticipantRequest(sessionId, memberId);

		// when
		var response = sessionParticipantService.removeParticipant(hostId, request);

		// then
		assertThat(response).isNotNull();
		verify(session).removeParticipant(memberId);
	}

	@Test
	@DisplayName("여러 HOST가 허용된다 - changeParticipantRole 테스트")
	void changeParticipantRole_MultipleHostsAllowed() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(courseQueryRepository.findManagedCourseById(courseId, managerId))
			.thenReturn(Optional.of(course));
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.HOST);

		// when
		var response = sessionParticipantService.changeParticipantRole(managerId, request);

		// then
		assertThat(response).isNotNull();
		verify(session).changeParticipantRole(memberId, SessionParticipantRole.HOST, clock);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("하위 세션에서 자가 탈퇴가 성공한다")
	void leaveSession_ChildSession_Success() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(false);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 하위 세션");
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new LeaveSessionRequest(sessionId);

		// when
		var response = sessionParticipantService.leaveSession(memberId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).removeParticipant(memberId);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("루트 세션에서 자가 탈퇴는 실패한다")
	void leaveSession_RootSession_ThrowsDomainException() {
		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(true);

		var request = new LeaveSessionRequest(sessionId);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.leaveSession(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.ROOT_SESSION_SELF_LEAVE_NOT_ALLOWED);

		verify(session, never()).removeParticipant(any());
		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("하위 세션에서 HOST 자가 탈퇴시 다른 HOST가 없으면 실패한다")
	void leaveSession_ChildSession_HostWithoutOtherHost_ThrowsDomainException() {
		// given
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(false);
		when(session.getParticipants()).thenReturn(List.of(hostParticipant));

		var request = new LeaveSessionRequest(sessionId);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.leaveSession(hostId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.HOST_CANNOT_LEAVE_ALONE);

		verify(session, never()).removeParticipant(any());
		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("하위 세션에서 HOST 자가 탈퇴시 다른 HOST가 있으면 성공한다")
	void leaveSession_ChildSession_HostWithOtherHost_Success() {
		// given
		var hostParticipant1 = mock(SessionParticipant.class);
		when(hostParticipant1.getMemberId()).thenReturn(hostId);
		when(hostParticipant1.getRole()).thenReturn(SessionParticipantRole.HOST);

		var hostParticipant2 = mock(SessionParticipant.class);
		when(hostParticipant2.getMemberId()).thenReturn(999L);
		when(hostParticipant2.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(false);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 하위 세션");
		when(session.getParticipants()).thenReturn(List.of(hostParticipant1, hostParticipant2));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new LeaveSessionRequest(sessionId);

		// when
		var response = sessionParticipantService.leaveSession(hostId, request);

		// then
		assertThat(response).isNotNull();
		verify(session).removeParticipant(hostId);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("존재하지 않는 세션에서 자가 탈퇴는 실패한다")
	void leaveSession_SessionNotFound_ThrowsDomainException() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

		var request = new LeaveSessionRequest(sessionId);

		// when & then
		assertThatThrownBy(() -> sessionParticipantService.leaveSession(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);
	}
}

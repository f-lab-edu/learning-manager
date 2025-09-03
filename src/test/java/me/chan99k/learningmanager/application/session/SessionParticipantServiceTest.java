package me.chan99k.learningmanager.application.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.AddParticipantRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.ChangeParticipantRoleRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.RemoveParticipantRequest;
import me.chan99k.learningmanager.application.session.requires.SessionCommandRepository;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionParticipant;
import me.chan99k.learningmanager.domain.session.SessionParticipantRole;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@ExtendWith(MockitoExtension.class)
class SessionParticipantServiceTest {

	private final Long sessionId = 1L;
	private final Long courseId = 100L;
	private final Long managerId = 200L;
	private final Long hostId = 300L;
	private final Long memberId = 400L;
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
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(managerId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(courseQueryRepository.findManagedCourseById(courseId, managerId))
				.thenReturn(Optional.of(course));
			when(session.getParticipants()).thenReturn(List.of());
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

			var response = sessionParticipantService.addParticipant(sessionId, request);

			assertThat(response).isNotNull();
			assertThat(response.sessionId()).isEqualTo(sessionId);
			verify(session).addParticipant(memberId, SessionParticipantRole.ATTENDEE);
			verify(sessionCommandRepository).save(session);
		}
	}

	@Test
	@DisplayName("Session Host는 참여자를 추가할 수 있다")
	void addParticipant_AsSessionHost_Success() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(hostId));

			var hostParticipant = mock(SessionParticipant.class);
			when(hostParticipant.getMemberId()).thenReturn(hostId);
			when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(session.getId()).thenReturn(sessionId); // 응답 생성용  
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(courseQueryRepository.findManagedCourseById(courseId, hostId))
				.thenReturn(Optional.empty()); // Course Manager 아님
			when(session.getParticipants()).thenReturn(List.of(hostParticipant));
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new AddParticipantRequest(memberId, SessionParticipantRole.SPEAKER);

			var response = sessionParticipantService.addParticipant(sessionId, request);

			assertThat(response).isNotNull();
			verify(session).addParticipant(memberId, SessionParticipantRole.SPEAKER);
		}
	}

	@Test
	@DisplayName("권한이 없는 사용자는 참여자를 추가할 수 없다")
	void addParticipant_NoPermission_ThrowsAuthorizationException() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(courseQueryRepository.findManagedCourseById(courseId, memberId))
				.thenReturn(Optional.empty()); // Course Manager 아님
			when(session.getParticipants()).thenReturn(List.of()); // Host 아님

			var request = new AddParticipantRequest(999L, SessionParticipantRole.ATTENDEE);

			assertThatThrownBy(() -> sessionParticipantService.addParticipant(sessionId, request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("인증되지 않은 사용자는 참여자를 추가할 수 없다")
	void addParticipant_NotAuthenticated_ThrowsAuthenticationException() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.empty());

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));

			var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

			assertThatThrownBy(() -> sessionParticipantService.addParticipant(sessionId, request))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("존재하지 않는 세션의 참여자를 추가할 수 없다")
	void addParticipant_SessionNotFound_ThrowsDomainException() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		assertThatThrownBy(() -> sessionParticipantService.addParticipant(sessionId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);
	}

	@Test
	@DisplayName("Course Manager는 참여자를 제거할 수 있다")
	void removeParticipant_AsCourseManager_Success() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(managerId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(courseQueryRepository.findManagedCourseById(courseId, managerId))
				.thenReturn(Optional.of(course));
			when(session.getParticipants()).thenReturn(List.of());
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new RemoveParticipantRequest(sessionId, memberId);

			// when
			var response = sessionParticipantService.removeParticipant(request);

			// then
			assertThat(response).isNotNull();
			assertThat(response.sessionId()).isEqualTo(sessionId);
			verify(session).removeParticipant(memberId);
			verify(sessionCommandRepository).save(session);
		}
	}

	@Test
	@DisplayName("Session Host는 참여자를 제거할 수 있다")
	void removeParticipant_AsSessionHost_Success() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(hostId));

			var hostParticipant = mock(SessionParticipant.class);
			when(hostParticipant.getMemberId()).thenReturn(hostId);
			when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(null); // standalone 세션 (Course 없음)
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(session.getParticipants()).thenReturn(List.of(hostParticipant));
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new RemoveParticipantRequest(sessionId, memberId);

			var response = sessionParticipantService.removeParticipant(request);

			assertThat(response).isNotNull();
			verify(session).removeParticipant(memberId);
		}
	}

	@Test
	@DisplayName("권한이 없는 사용자는 참여자를 제거할 수 없다")
	void removeParticipant_NoPermission_ThrowsAuthorizationException() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(courseQueryRepository.findManagedCourseById(courseId, memberId))
				.thenReturn(Optional.empty()); // Course Manager 아님
			when(session.getParticipants()).thenReturn(List.of()); // Host 아님

			var request = new RemoveParticipantRequest(sessionId, 999L);

			assertThatThrownBy(() -> sessionParticipantService.removeParticipant(request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("Course Manager는 참여자 역할을 변경할 수 있다")
	void changeParticipantRole_AsCourseManager_Success() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(managerId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(courseQueryRepository.findManagedCourseById(courseId, managerId))
				.thenReturn(Optional.of(course));
			when(session.getParticipants()).thenReturn(List.of());
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.SPEAKER);


			var response = sessionParticipantService.changeParticipantRole(request);

			assertThat(response).isNotNull();
			assertThat(response.sessionId()).isEqualTo(sessionId);
			verify(session).changeParticipantRole(memberId, SessionParticipantRole.SPEAKER);
			verify(sessionCommandRepository).save(session);
		}
	}

	@Test
	@DisplayName("Session Host는 참여자 역할을 변경할 수 있다")
	void changeParticipantRole_AsSessionHost_Success() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(hostId));

			var hostParticipant = mock(SessionParticipant.class);
			when(hostParticipant.getMemberId()).thenReturn(hostId);
			when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(null); // standalone 세션 (Course 없음)
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(session.getParticipants()).thenReturn(List.of(hostParticipant));
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.ATTENDEE);

			var response = sessionParticipantService.changeParticipantRole(request);

			assertThat(response).isNotNull();
			verify(session).changeParticipantRole(memberId, SessionParticipantRole.ATTENDEE);
		}
	}

	@Test
	@DisplayName("권한이 없는 사용자는 참여자 역할을 변경할 수 없다")
	void changeParticipantRole_NoPermission_ThrowsAuthorizationException() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(courseQueryRepository.findManagedCourseById(courseId, memberId))
				.thenReturn(Optional.empty()); // Course Manager 아님
			when(session.getParticipants()).thenReturn(List.of()); // Host 아님

			var request = new ChangeParticipantRoleRequest(sessionId, 999L, SessionParticipantRole.SPEAKER);

			assertThatThrownBy(() -> sessionParticipantService.changeParticipantRole(request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("단독 세션(Course 없음)의 경우 Host만 참여자 관리가 가능하다")
	void addParticipant_StandaloneSession_OnlyHostCanManage() {
		when(session.getCourseId()).thenReturn(null); // 단독 세션

		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(hostId));

			var hostParticipant = mock(SessionParticipant.class);
			when(hostParticipant.getMemberId()).thenReturn(hostId);
			when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getParticipants()).thenReturn(List.of(hostParticipant));
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

			var response = sessionParticipantService.addParticipant(sessionId, request);

			assertThat(response).isNotNull();
			verify(session).addParticipant(memberId, SessionParticipantRole.ATTENDEE);
			// Course 조회는 발생하지 않아야 함
			verifyNoInteractions(courseQueryRepository);
		}
	}

	@Test
	@DisplayName("단독 세션에서 Host가 아닌 사용자는 참여자 관리를 할 수 없다")
	void addParticipant_StandaloneSession_NonHostCannotManage() {
		when(session.getCourseId()).thenReturn(null); // 단독 세션

		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getParticipants()).thenReturn(List.of()); // Host 아님

			var request = new AddParticipantRequest(999L, SessionParticipantRole.ATTENDEE);

			assertThatThrownBy(() -> sessionParticipantService.addParticipant(sessionId, request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			// Course 조회는 발생하지 않아야 함
			verifyNoInteractions(courseQueryRepository);
		}
	}

	@Test
	@DisplayName("HOST 자신을 제거할 때 다른 HOST가 있으면 성공한다")
	void removeParticipant_HostSelfRemoval_SuccessWithOtherHost() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(hostId));

			var hostParticipant1 = mock(SessionParticipant.class);
			when(hostParticipant1.getMemberId()).thenReturn(hostId);
			when(hostParticipant1.getRole()).thenReturn(SessionParticipantRole.HOST);

			var hostParticipant2 = mock(SessionParticipant.class);
			when(hostParticipant2.getMemberId()).thenReturn(999L);
			when(hostParticipant2.getRole()).thenReturn(SessionParticipantRole.HOST);

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(null); // standalone 세션 (Course 없음)
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(session.getParticipants()).thenReturn(List.of(hostParticipant1, hostParticipant2));
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new RemoveParticipantRequest(sessionId, hostId); // 자신을 제거

			var response = sessionParticipantService.removeParticipant(request);

			assertThat(response).isNotNull();
			verify(session).removeParticipant(hostId);
		}
	}

	@Test
	@DisplayName("HOST 자신을 제거할 때 다른 HOST가 없으면 실패한다")
	void removeParticipant_HostSelfRemoval_FailWithoutOtherHost() {
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(hostId));

			var hostParticipant = mock(SessionParticipant.class);
			when(hostParticipant.getMemberId()).thenReturn(hostId);
			when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

			var attendeeParticipant = mock(SessionParticipant.class);

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(null); // standalone 세션 (Course 없음)
			when(session.getParticipants()).thenReturn(List.of(hostParticipant, attendeeParticipant));

			var request = new RemoveParticipantRequest(sessionId, hostId); // 자신을 제거

			assertThatThrownBy(() -> sessionParticipantService.removeParticipant(request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.HOST_CANNOT_LEAVE_ALONE);

			verify(session, never()).removeParticipant(hostId);
		}
	}

	@Test
	@DisplayName("HOST가 아닌 사용자를 제거하는 것은 자유롭게 가능하다")
	void removeParticipant_NonHostRemoval_Success() {
		// given
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(hostId));

			var hostParticipant = mock(SessionParticipant.class);
			when(hostParticipant.getMemberId()).thenReturn(hostId);
			when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(null); // standalone 세션 (Course 없음)
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(session.getParticipants()).thenReturn(List.of(hostParticipant));
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new RemoveParticipantRequest(sessionId, memberId); // 다른 사용자 제거

			// when
			var response = sessionParticipantService.removeParticipant(request);

			// then
			assertThat(response).isNotNull();
			verify(session).removeParticipant(memberId);
		}
	}

	@Test
	@DisplayName("여러 HOST가 허용된다 - changeParticipantRole 테스트")
	void changeParticipantRole_MultipleHostsAllowed() {
		// given
		try (var mockedStatic = mockStatic(AuthenticationContextHolder.class)) {
			mockedStatic.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(managerId));

			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(courseId); // 권한 확인용
			when(session.getId()).thenReturn(sessionId); // 응답 생성용
			when(session.getTitle()).thenReturn("테스트 세션"); // 응답 생성용
			when(courseQueryRepository.findManagedCourseById(courseId, managerId))
				.thenReturn(Optional.of(course));
			when(session.getParticipants()).thenReturn(List.of());
			when(sessionCommandRepository.save(session)).thenReturn(session);

			var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.HOST);

			// when
			var response = sessionParticipantService.changeParticipantRole(request);

			// then
			assertThat(response).isNotNull();
			verify(session).changeParticipantRole(memberId, SessionParticipantRole.HOST);
			verify(sessionCommandRepository).save(session);
		}
	}
}
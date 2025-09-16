package me.chan99k.learningmanager.application.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.application.session.requires.SessionCommandRepository;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.SystemRole;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@ExtendWith(MockitoExtension.class)
class SessionDeletionServiceTest {

	@InjectMocks
	private SessionDeletionService sessionDeletionService;

	@Mock
	private SessionQueryRepository sessionQueryRepository;

	@Mock
	private SessionCommandRepository sessionCommandRepository;

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private Session session;

	@Mock
	private Course course;

	@Mock
	private Member member;

	@Test
	@DisplayName("[Success] 과정 관리자가 과정 세션 삭제에 성공한다")
	void deleteSession_CourseSession_Success() {
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(member));
			when(session.getCourseId()).thenReturn(courseId);
			when(session.getChildren()).thenReturn(Collections.emptyList());
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			sessionDeletionService.deleteSession(sessionId);

			verify(sessionCommandRepository).delete(session);
		}
	}

	@Test
	@DisplayName("[Success] 시스템 관리자가 단독 세션 삭제에 성공한다")
	void deleteSession_StandaloneSession_Success() {
		long sessionId = 1L;
		long adminId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(adminId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(member));
			when(session.getCourseId()).thenReturn(null);
			when(session.getCurriculumId()).thenReturn(null);
			when(session.getChildren()).thenReturn(Collections.emptyList());
			when(member.getRole()).thenReturn(SystemRole.ADMIN);

			sessionDeletionService.deleteSession(sessionId);

			verify(sessionCommandRepository).delete(session);
		}
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 AuthenticationException이 발생한다")
	void deleteSession_Fail_Unauthenticated() {
		long sessionId = 1L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sessionDeletionService.deleteSession(sessionId))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);

			verify(sessionQueryRepository, never()).findById(anyLong());
			verify(sessionCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션 삭제 시 DomainException이 발생한다")
	void deleteSession_Fail_SessionNotFound() {
		long sessionId = 999L;
		long managerId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sessionDeletionService.deleteSession(sessionId))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);

			verify(sessionCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 AuthorizationException이 발생한다")
	void deleteSession_Fail_NotCourseManager() {
		long sessionId = 1L;
		long courseId = 10L;
		long nonManagerId = 101L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(nonManagerId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(memberQueryRepository.findById(nonManagerId)).thenReturn(Optional.of(member));
			when(session.getCourseId()).thenReturn(courseId);
			when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sessionDeletionService.deleteSession(sessionId))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			verify(sessionCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Failure] 단독 세션을 일반 사용자가 삭제하려 하면 AuthorizationException이 발생한다")
	void deleteSession_Fail_StandaloneSessionNotAdmin() {
		long sessionId = 1L;
		long userId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(userId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(memberQueryRepository.findById(userId)).thenReturn(Optional.of(member));
			when(session.getCourseId()).thenReturn(null);
			when(session.getCurriculumId()).thenReturn(null);
			when(member.getRole()).thenReturn(SystemRole.MEMBER);

			assertThatThrownBy(() -> sessionDeletionService.deleteSession(sessionId))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			verify(sessionCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Failure] 하위 세션이 있는 세션 삭제 시 IllegalArgumentException이 발생한다")
	void deleteSession_Fail_SessionWithChildren() {
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;
		Session childSession = mock(Session.class);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(member));
			when(session.getCourseId()).thenReturn(courseId);
			when(session.getChildren()).thenReturn(List.of(childSession));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			assertThatThrownBy(() -> sessionDeletionService.deleteSession(sessionId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("하위 세션이 있는 세션은 삭제할 수 없습니다");

			verify(sessionCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 사용자면 DomainException이 발생한다")
	void deleteSession_Fail_MemberNotFound() {
		long sessionId = 1L;
		long invalidMemberId = 999L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(invalidMemberId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(memberQueryRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sessionDeletionService.deleteSession(sessionId))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

			verify(sessionCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Behavior] sessionCommandRepository.delete()가 호출되는지 확인한다")
	void deleteSession_VerifyRepositoryDelete() {
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
			when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(member));
			when(session.getCourseId()).thenReturn(courseId);
			when(session.getChildren()).thenReturn(Collections.emptyList());
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			sessionDeletionService.deleteSession(sessionId);

			verify(sessionCommandRepository).delete(session);
		}
	}
}
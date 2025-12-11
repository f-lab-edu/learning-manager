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
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionCommandRepository;
import me.chan99k.learningmanager.session.SessionDeletionService;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;

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
	private SystemAuthorizationPort systemAuthorizationPort;

	@Mock
	private Session session;

	@Mock
	private Course course;

	@Test
	@DisplayName("[Success] 과정 관리자가 과정 세션 삭제에 성공한다")
	void deleteSession_CourseSession_Success() {
		// given
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getChildren()).thenReturn(Collections.emptyList());
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		sessionDeletionService.deleteSession(managerId, sessionId);

		// then
		verify(sessionCommandRepository).delete(session);
	}

	@Test
	@DisplayName("[Success] 시스템 관리자가 단독 세션 삭제에 성공한다")
	void deleteSession_StandaloneSession_Success() {
		long sessionId = 1L;
		long adminId = 100L;

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(session.getCurriculumId()).thenReturn(null);
		when(session.getChildren()).thenReturn(Collections.emptyList());
		when(systemAuthorizationPort.hasRole(adminId, SystemRole.ADMIN)).thenReturn(true);

		// when
		sessionDeletionService.deleteSession(adminId, sessionId);

		// then
		verify(sessionCommandRepository).delete(session);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션 삭제 시 DomainException이 발생한다")
	void deleteSession_Fail_SessionNotFound() {
		long sessionId = 999L;
		long managerId = 100L;

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> sessionDeletionService.deleteSession(managerId, sessionId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);

		verify(sessionCommandRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 DomainException이 발생한다")
	void deleteSession_Fail_NotCourseManager() {
		long sessionId = 1L;
		long courseId = 10L;
		long nonManagerId = 101L;

		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> sessionDeletionService.deleteSession(nonManagerId, sessionId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		verify(sessionCommandRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Failure] 단독 세션을 일반 사용자가 삭제하려 하면 DomainException이 발생한다")
	void deleteSession_Fail_StandaloneSessionNotAdmin() {
		long sessionId = 1L;
		long userId = 100L;

		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(session.getCurriculumId()).thenReturn(null);
		when(systemAuthorizationPort.hasRole(userId, SystemRole.ADMIN)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> sessionDeletionService.deleteSession(userId, sessionId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.ADMIN_ONLY_ACTION);

		verify(sessionCommandRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Failure] 하위 세션이 있는 세션 삭제 시 IllegalArgumentException이 발생한다")
	void deleteSession_Fail_SessionWithChildren() {
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;
		Session childSession = mock(Session.class);

		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getChildren()).thenReturn(List.of(childSession));
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when & then
		assertThatThrownBy(() -> sessionDeletionService.deleteSession(managerId, sessionId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("하위 세션이 있는 세션은 삭제할 수 없습니다");

		verify(sessionCommandRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Behavior] sessionCommandRepository.delete()가 호출되는지 확인한다")
	void deleteSession_VerifyRepositoryDelete() {
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;

		// given
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getChildren()).thenReturn(Collections.emptyList());
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		sessionDeletionService.deleteSession(managerId, sessionId);

		// then
		verify(sessionCommandRepository).delete(session);
	}
}

package me.chan99k.learningmanager.application.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.application.auth.UserContext;
import me.chan99k.learningmanager.application.course.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.MemberQueryRepository;
import me.chan99k.learningmanager.application.session.provides.SessionUpdate;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthenticationException;
import me.chan99k.learningmanager.domain.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.SystemRole;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;
import me.chan99k.learningmanager.domain.session.SessionType;

@ExtendWith(MockitoExtension.class)
class SessionUpdateServiceTest {

	@Mock
	private Clock clock;

	@InjectMocks
	private SessionUpdateService sessionUpdateService;

	@Mock
	private SessionQueryRepository sessionQueryRepository;

	@Mock
	private SessionCommandRepository sessionCommandRepository;

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private UserContext userContext;

	@Mock
	private Session session;

	@Mock
	private Course course;

	@Mock
	private Member member;

	@Test
	@DisplayName("[Success] 과정 관리자가 과정 세션 수정에 성공한다")
	void updateSession_CourseSession_Success() {
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;
		Instant scheduledAt = LocalDateTime.now().plusDays(7).toInstant(ZoneOffset.UTC);
		Instant scheduledEndAt = LocalDateTime.now().plusDays(7).plusHours(2).toInstant(ZoneOffset.UTC);

		SessionUpdate.Request request = new SessionUpdate.Request(
			"Updated Session Title",
			scheduledAt,
			scheduledEndAt,
			SessionType.ONLINE,
			SessionLocation.ZOOM,
			null
		);

		// given
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(member));
		when(session.getCourseId()).thenReturn(courseId);
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		sessionUpdateService.updateSession(sessionId, request);

		// then
		verify(session).reschedule(scheduledAt, scheduledEndAt, clock);
		verify(session).changeInfo("Updated Session Title", SessionType.ONLINE, clock);
		verify(session).changeLocation(SessionLocation.ZOOM, null, clock);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Success] 시스템 관리자가 단독 세션 수정에 성공한다")
	void updateSession_StandaloneSession_Success() {
		long sessionId = 1L;
		long adminId = 100L;
		Instant scheduledAt = LocalDateTime.now().plusDays(5).toInstant(ZoneOffset.UTC);
		Instant scheduledEndAt = LocalDateTime.now().plusDays(5).plusHours(3).toInstant(ZoneOffset.UTC);

		SessionUpdate.Request request = new SessionUpdate.Request(
			"Updated Standalone Session",
			scheduledAt,
			scheduledEndAt,
			SessionType.OFFLINE,
			SessionLocation.SITE,
			"Conference Room A"
		);

		// given
		when(userContext.getCurrentMemberId()).thenReturn(adminId);
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(member));
		when(session.getCourseId()).thenReturn(null);
		when(session.getCurriculumId()).thenReturn(null);
		when(member.getRole()).thenReturn(SystemRole.ADMIN);

		// when
		sessionUpdateService.updateSession(sessionId, request);

		// then
		verify(session).reschedule(scheduledAt, scheduledEndAt, clock);
		verify(session).changeInfo("Updated Standalone Session", SessionType.OFFLINE, clock);
		verify(session).changeLocation(SessionLocation.SITE, "Conference Room A", clock);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 AuthenticationException이 발생한다")
	void updateSession_Fail_Unauthenticated() {
		long sessionId = 1L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		// given
		when(userContext.getCurrentMemberId()).thenThrow(
			new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> sessionUpdateService.updateSession(sessionId, request))
			.isInstanceOf(AuthenticationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);

		verify(sessionQueryRepository, never()).findById(anyLong());
		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션 수정 시 DomainException이 발생한다")
	void updateSession_Fail_SessionNotFound() {
		long sessionId = 999L;
		long managerId = 100L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		// given
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> sessionUpdateService.updateSession(sessionId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);

		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 AuthorizationException이 발생한다")
	void updateSession_Fail_NotCourseManager() {
		long sessionId = 1L;
		long courseId = 10L;
		long nonManagerId = 101L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		// given
		when(userContext.getCurrentMemberId()).thenReturn(nonManagerId);
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(memberQueryRepository.findById(nonManagerId)).thenReturn(Optional.of(member));
		when(session.getCourseId()).thenReturn(courseId);
		when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> sessionUpdateService.updateSession(sessionId, request))
			.isInstanceOf(AuthorizationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 단독 세션을 일반 사용자가 수정하려 하면 AuthorizationException이 발생한다")
	void updateSession_Fail_StandaloneSessionNotAdmin() {
		long sessionId = 1L;
		long userId = 100L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		// given
		when(userContext.getCurrentMemberId()).thenReturn(userId);
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(memberQueryRepository.findById(userId)).thenReturn(Optional.of(member));
		when(session.getCourseId()).thenReturn(null);
		when(session.getCurriculumId()).thenReturn(null);
		when(member.getRole()).thenReturn(SystemRole.MEMBER);

		// when & then
		assertThatThrownBy(() -> sessionUpdateService.updateSession(sessionId, request))
			.isInstanceOf(AuthorizationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 사용자면 DomainException이 발생한다")
	void updateSession_Fail_MemberNotFound() {
		long sessionId = 1L;
		long invalidMemberId = 999L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		// given
		when(userContext.getCurrentMemberId()).thenReturn(invalidMemberId);
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(memberQueryRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> sessionUpdateService.updateSession(sessionId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Behavior] sessionCommandRepository.update()가 호출되는지 확인한다")
	void updateSession_VerifyRepositoryUpdate() {
		long sessionId = 1L;
		long courseId = 10L;
		long managerId = 100L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		// given
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(member));
		when(session.getCourseId()).thenReturn(courseId);
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		sessionUpdateService.updateSession(sessionId, request);

		// then
		verify(sessionCommandRepository).save(session);
	}
}
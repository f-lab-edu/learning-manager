package me.chan99k.learningmanager.application.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.application.auth.requires.UserContext;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.application.session.provides.SessionCreation;
import me.chan99k.learningmanager.application.session.requires.SessionCommandRepository;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseProblemCode;
import me.chan99k.learningmanager.domain.course.Curriculum;
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
class SessionCreationServiceTest {

	@Mock
	private Clock clock;

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

	@InjectMocks
	private SessionCreationService sessionCreationService;

	@Test
	@DisplayName("[Success] 시스템 관리자가 스탠드얼론 세션 생성에 성공한다")
	void createStandaloneSession_Success() {
		// given
		Instant fixedTime = Instant.parse("2024-01-15T10:00:00Z");
		lenient().when(clock.instant()).thenReturn(fixedTime);
		lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));

		Long adminId = 1L;
		Member admin = mock(Member.class);
		when(admin.getRole()).thenReturn(SystemRole.ADMIN);
		when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(admin));

		Instant startTime = fixedTime.plusSeconds(86400); // +1 day
		Instant endTime = fixedTime.plusSeconds(86400 + 7200); // +1 day +2 hours

		SessionCreation.Request request = new SessionCreation.Request(
			null, null, null,
			"테스트 세션",
			startTime,
			endTime,
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		Session mockSession = mock(Session.class);
		when(sessionCommandRepository.create(any(Session.class))).thenReturn(mockSession);
		when(userContext.getCurrentMemberId()).thenReturn(adminId);

		// when
		Session result = sessionCreationService.createSession(request);

		// then
		assertThat(result).isEqualTo(mockSession);
		verify(sessionCommandRepository).create(any(Session.class));
	}

	@Test
	@DisplayName("[Failure] 비관리자가 스탠드얼론 세션 생성 시 권한 예외 발생")
	void createStandaloneSession_AuthorizationFail() {
		// given
		Long userId = 1L;
		Member user = mock(Member.class);
		when(user.getRole()).thenReturn(SystemRole.MEMBER);
		when(memberQueryRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userContext.getCurrentMemberId()).thenReturn(userId);

		SessionCreation.Request request = new SessionCreation.Request(
			null, null, null,
			"테스트 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, "Zoom 링크"
		);

		// when & then
		assertThatThrownBy(() -> sessionCreationService.createSession(request))
			.isInstanceOf(AuthorizationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 과정 세션 생성에 성공한다")
	void createCourseSession_Success() {
		// given
		Instant fixedTime = Instant.parse("2024-01-15T10:00:00Z");
		lenient().when(clock.instant()).thenReturn(fixedTime);
		lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));

		Long managerId = 1L;
		Long courseId = 1L;
		Member manager = mock(Member.class);
		Course course = mock(Course.class);

		when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(userContext.getCurrentMemberId()).thenReturn(managerId);

		Instant startTime = fixedTime.plusSeconds(86400); // +1 day
		Instant endTime = fixedTime.plusSeconds(86400 + 7200); // +1 day +2 hours

		SessionCreation.Request request = new SessionCreation.Request(
			courseId, null, null,
			"과정 세션",
			startTime,
			endTime,
			SessionType.OFFLINE, SessionLocation.SITE, "강의실 A"
		);

		Session mockSession = mock(Session.class);
		when(sessionCommandRepository.create(any(Session.class))).thenReturn(mockSession);

		// when
		Session result = sessionCreationService.createSession(request);

		// then
		assertThat(result).isEqualTo(mockSession);
		verify(courseQueryRepository).findManagedCourseById(courseId, managerId);
		verify(sessionCommandRepository).create(any(Session.class));
	}

	@Test
	@DisplayName("[Failure] 과정에 권한이 없는 사용자가 과정 세션 생성 시 권한 예외 발생")
	void createCourseSession_AuthorizationFail() {
		// given
		Long userId = 1L;
		Long courseId = 1L;
		Member user = mock(Member.class);

		when(memberQueryRepository.findById(userId)).thenReturn(Optional.of(user));
		when(courseQueryRepository.findManagedCourseById(courseId, userId)).thenReturn(Optional.empty());
		when(userContext.getCurrentMemberId()).thenReturn(userId);

		SessionCreation.Request request = new SessionCreation.Request(
			courseId, null, null,
			"과정 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.OFFLINE, SessionLocation.SITE, "강의실 A"
		);

		// when & then
		assertThatThrownBy(() -> sessionCreationService.createSession(request))
			.isInstanceOf(AuthorizationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 커리큘럼 세션 생성에 성공한다")
	void createCurriculumSession_Success() {
		// given
		Instant fixedTime = Instant.parse("2024-01-15T10:00:00Z");
		lenient().when(clock.instant()).thenReturn(fixedTime);
		lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));

		Long managerId = 1L;
		Long courseId = 1L;
		Long curriculumId = 1L;
		Member manager = mock(Member.class);
		Course course = mock(Course.class);
		Curriculum curriculum = mock(Curriculum.class);

		when(curriculum.getId()).thenReturn(curriculumId);
		when(course.getCurriculumList()).thenReturn(List.of(curriculum));
		when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(userContext.getCurrentMemberId()).thenReturn(managerId);

		Instant startTime = fixedTime.plusSeconds(86400); // +1 day
		Instant endTime = fixedTime.plusSeconds(86400 + 7200); // +1 day +2 hours

		SessionCreation.Request request = new SessionCreation.Request(
			courseId, curriculumId, null,
			"커리큘럼 세션",
			startTime,
			endTime,
			SessionType.OFFLINE, SessionLocation.SITE, "강의실 B"
		);

		Session mockSession = mock(Session.class);
		when(sessionCommandRepository.create(any(Session.class))).thenReturn(mockSession);

		// when
		Session result = sessionCreationService.createSession(request);

		// then
		assertThat(result).isEqualTo(mockSession);
		verify(courseQueryRepository).findManagedCourseById(courseId, managerId);
		verify(sessionCommandRepository).create(any(Session.class));
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 커리큘럼으로 세션 생성 시 도메인 예외 발생")
	void createCurriculumSession_CurriculumNotFound() {
		// given
		Long managerId = 1L;
		Long courseId = 1L;
		Long invalidCurriculumId = 999L;
		Member manager = mock(Member.class);
		Course course = mock(Course.class);

		when(course.getCurriculumList()).thenReturn(List.of());
		when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(userContext.getCurrentMemberId()).thenReturn(managerId);

		SessionCreation.Request request = new SessionCreation.Request(
			courseId, invalidCurriculumId, null,
			"커리큘럼 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.OFFLINE,
			SessionLocation.SITE,
			"강의실 B"
		);

		// when & then
		assertThatThrownBy(() -> sessionCreationService.createSession(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.CURRICULUM_NOT_FOUND_IN_COURSE);
	}

	@Test
	@DisplayName("[Success] 부모 세션의 하위 세션 생성에 성공한다")
	void createChildSession_Success() {
		// given
		Long managerId = 1L;
		Long parentSessionId = 1L;
		Member manager = mock(Member.class);
		Session parentSession = mock(Session.class);
		Session childSession = mock(Session.class);

		when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(sessionQueryRepository.findById(parentSessionId)).thenReturn(Optional.of(parentSession));
		when(parentSession.createChildSession(anyString(), any(Instant.class), any(Instant.class),
			any(SessionType.class), any(SessionLocation.class), anyString(), any(Clock.class))).thenReturn(
			childSession);
		when(sessionCommandRepository.create(childSession)).thenReturn(childSession);
		when(userContext.getCurrentMemberId()).thenReturn(managerId);

		SessionCreation.Request request = new SessionCreation.Request(
			null, null, parentSessionId,
			"하위 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE,
			SessionLocation.ZOOM,
			"Zoom 링크"
		);

		// when
		Session result = sessionCreationService.createSession(request);

		// then
		assertThat(result).isEqualTo(childSession);
		verify(sessionQueryRepository).findById(parentSessionId);
		verify(parentSession).createChildSession(anyString(), any(Instant.class), any(Instant.class),
			any(SessionType.class), any(SessionLocation.class), anyString(), any(Clock.class));
		verify(sessionCommandRepository).create(childSession);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 부모 세션으로 하위 세션 생성 시 도메인 예외 발생")
	void createChildSession_ParentNotFound() {
		Long managerId = 1L;
		Long invalidParentSessionId = 999L;
		Member manager = mock(Member.class);

		when(memberQueryRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(sessionQueryRepository.findById(invalidParentSessionId)).thenReturn(Optional.empty());

		SessionCreation.Request request = new SessionCreation.Request(
			null, null, invalidParentSessionId,
			"하위 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE,
			SessionLocation.ZOOM,
			"Zoom 링크"
		);

		when(userContext.getCurrentMemberId()).thenReturn(managerId);

		// when & then
		assertThatThrownBy(() -> sessionCreationService.createSession(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자의 세션 생성 시 인증 예외 발생")
	void createSession_AuthenticationFail() {
		SessionCreation.Request request = new SessionCreation.Request(
			null, null, null,
			"테스트 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, "Zoom 링크"
		);

		when(userContext.getCurrentMemberId()).thenThrow(
			new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> sessionCreationService.createSession(request))
			.isInstanceOf(AuthenticationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 사용자의 세션 생성 시 도메인 예외 발생")
	void createSession_MemberNotFound() {
		Long invalidMemberId = 999L;
		when(memberQueryRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

		SessionCreation.Request request = new SessionCreation.Request(
			null, null, null,
			"테스트 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, "Zoom 링크"
		);

		when(userContext.getCurrentMemberId()).thenReturn(invalidMemberId);

		// when & then
		assertThatThrownBy(() -> sessionCreationService.createSession(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);
	}
}
package me.chan99k.learningmanager.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionParticipant;
import me.chan99k.learningmanager.session.SessionParticipantRole;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionSecurityService 테스트")
class SessionSecurityServiceTest {

	private static final Long SESSION_ID = 1L;
	private static final Long COURSE_ID = 10L;
	private static final Long MEMBER_ID = 100L;

	@InjectMocks
	private SessionSecurityService sessionSecurityService;

	@Mock
	private SessionAuthorizationPort sessionAuthorizationPort;

	@Mock
	private SessionQueryRepository sessionQueryRepository;

	@Mock
	private SystemAuthorizationPort systemAuthorizationPort;

	@Mock
	private Session session;

	static Stream<Arguments> allSecurityMethods() {
		return Stream.of(
			Arguments.of("isSessionManager",
				(BiFunction<SessionSecurityService, Long[], Boolean>)(svc, args) ->
					svc.isSessionManager(args[0], args[1])),
			Arguments.of("isSessionManagerOrMentor",
				(BiFunction<SessionSecurityService, Long[], Boolean>)(svc, args) ->
					svc.isSessionManagerOrMentor(args[0], args[1])),
			Arguments.of("isSessionMember",
				(BiFunction<SessionSecurityService, Long[], Boolean>)(svc, args) ->
					svc.isSessionMember(args[0], args[1])),
			Arguments.of("canManageSessionParticipants",
				(BiFunction<SessionSecurityService, Long[], Boolean>)(svc, args) ->
					svc.canManageSessionParticipants(args[0], args[1]))
		);
	}

	@ParameterizedTest(name = "{0}: 세션이 존재하지 않으면 false를 반환한다")
	@MethodSource("allSecurityMethods")
	@DisplayName("[Failure] 세션이 존재하지 않으면 false를 반환한다")
	void test01(String methodName, BiFunction<SessionSecurityService, Long[], Boolean> method) {
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		boolean result = method.apply(sessionSecurityService, new Long[] {SESSION_ID, MEMBER_ID});

		assertThat(result).isFalse();
	}

	@ParameterizedTest(name = "{0}: 단독 세션에서 OPERATOR 이상 권한이 있으면 true를 반환한다")
	@MethodSource("allSecurityMethods")
	@DisplayName("[Success] 단독 세션에서 OPERATOR 이상 권한이 있으면 true를 반환한다")
	void test02(String methodName, BiFunction<SessionSecurityService, Long[], Boolean> method) {
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(systemAuthorizationPort.hasRoleOrHigher(MEMBER_ID, SystemRole.OPERATOR)).thenReturn(true);

		boolean result = method.apply(sessionSecurityService, new Long[] {SESSION_ID, MEMBER_ID});

		assertThat(result).isTrue();
	}

	@ParameterizedTest(name = "{0}: 단독 세션에서 OPERATOR 미만 권한이면 false를 반환한다")
	@MethodSource("allSecurityMethods")
	@DisplayName("[Failure] 단독 세션에서 OPERATOR 미만 권한이면 false를 반환한다")
	void test03(String methodName, BiFunction<SessionSecurityService, Long[], Boolean> method) {
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		when(session.getCourseId()).thenReturn(null);
		when(systemAuthorizationPort.hasRoleOrHigher(MEMBER_ID, SystemRole.OPERATOR)).thenReturn(false);

		boolean result = method.apply(sessionSecurityService, new Long[] {SESSION_ID, MEMBER_ID});

		assertThat(result).isFalse();
	}

	@Nested
	@DisplayName("isSessionManager - 과정 세션")
	class IsSessionManagerCourseSessionTest {

		@Test
		@DisplayName("[Success] MANAGER 권한이 있으면 true를 반환한다")
		void test01() {
			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.hasRoleForCourse(MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
				.thenReturn(true);

			boolean result = sessionSecurityService.isSessionManager(SESSION_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] MANAGER 권한이 없으면 false를 반환한다")
		void test02() {
			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.hasRoleForCourse(MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
				.thenReturn(false);

			boolean result = sessionSecurityService.isSessionManager(SESSION_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("isSessionManagerOrMentor - 과정 세션")
	class IsSessionManagerOrMentorCourseSessionTest {

		@Test
		@DisplayName("[Success] MANAGER 또는 MENTOR 권한이 있으면 true를 반환한다")
		void test01() {
			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.hasAnyRoleForCourse(
				MEMBER_ID, COURSE_ID, List.of(CourseRole.MANAGER, CourseRole.MENTOR)
			)).thenReturn(true);

			boolean result = sessionSecurityService.isSessionManagerOrMentor(SESSION_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] MANAGER/MENTOR 권한이 없으면 false를 반환한다")
		void test02() {
			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.hasAnyRoleForCourse(
				MEMBER_ID, COURSE_ID, List.of(CourseRole.MANAGER, CourseRole.MENTOR)
			)).thenReturn(false);

			boolean result = sessionSecurityService.isSessionManagerOrMentor(SESSION_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("isSessionMember - 과정 세션")
	class IsSessionMemberCourseSessionTest {

		@Test
		@DisplayName("[Success] 과정 멤버이면 true를 반환한다")
		void test01() {
			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.isMemberOfCourse(MEMBER_ID, COURSE_ID)).thenReturn(true);

			boolean result = sessionSecurityService.isSessionMember(SESSION_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 과정 멤버가 아니면 false를 반환한다")
		void test02() {
			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.isMemberOfCourse(MEMBER_ID, COURSE_ID)).thenReturn(false);

			boolean result = sessionSecurityService.isSessionMember(SESSION_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("canManageSessionParticipants - 과정 세션")
	class CanManageSessionParticipantsCourseSessionTest {

		@Test
		@DisplayName("[Success] MANAGER 권한이 있으면 true를 반환한다")
		void test01() {
			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.hasRoleForCourse(MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
				.thenReturn(true);

			boolean result = sessionSecurityService.canManageSessionParticipants(SESSION_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Success] HOST 역할이면 true를 반환한다")
		void test02() {
			SessionParticipant hostParticipant = mock(SessionParticipant.class);
			when(hostParticipant.getMemberId()).thenReturn(MEMBER_ID);
			when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.hasRoleForCourse(MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
				.thenReturn(false);
			when(session.getParticipants()).thenReturn(List.of(hostParticipant));

			boolean result = sessionSecurityService.canManageSessionParticipants(SESSION_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] MANAGER도 HOST도 아니면 false를 반환한다")
		void test03() {
			SessionParticipant attendeeParticipant = mock(SessionParticipant.class);
			when(attendeeParticipant.getMemberId()).thenReturn(MEMBER_ID);
			when(attendeeParticipant.getRole()).thenReturn(SessionParticipantRole.ATTENDEE);

			when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
			when(session.getCourseId()).thenReturn(COURSE_ID);
			when(sessionAuthorizationPort.hasRoleForCourse(MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
				.thenReturn(false);
			when(session.getParticipants()).thenReturn(List.of(attendeeParticipant));

			boolean result = sessionSecurityService.canManageSessionParticipants(SESSION_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}
	}
}

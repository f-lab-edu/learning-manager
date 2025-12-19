package me.chan99k.learningmanager.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.JpaSessionRepository;
import me.chan99k.learningmanager.session.entity.SessionEntity;

@DisplayName("JpaSessionAuthorizationAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class JpaSessionAuthorizationAdapterTest {

	private static final Long MEMBER_ID = 1L;
	private static final Long SESSION_ID = 100L;
	private static final Long COURSE_ID = 10L;
	@Mock
	private JpaSessionRepository sessionRepository;
	@Mock
	private CourseAuthorizationPort courseAuthorizationPort;
	private JpaSessionAuthorizationAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new JpaSessionAuthorizationAdapter(sessionRepository, courseAuthorizationPort);
	}

	private SessionEntity createSessionEntityWithCourseId() {
		SessionEntity entity = new SessionEntity();
		entity.setId(SESSION_ID);
		entity.setCourseId(COURSE_ID);
		return entity;
	}

	@Nested
	@DisplayName("세션 ID 기반 메서드")
	class SessionIdBasedTests {

		@Test
		@DisplayName("[Success] hasRoleForSession으로 세션에 대한 역할을 확인한다")
		void test01() {
			SessionEntity entity = createSessionEntityWithCourseId();
			when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(entity));
			when(courseAuthorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER)).thenReturn(true);

			boolean result = adapter.hasRoleForSession(MEMBER_ID, SESSION_ID, CourseRole.MANAGER);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] hasRoleForSession으로 세션이 존재하지 않으면 false 반환")
		void test02() {
			when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

			boolean result = adapter.hasRoleForSession(MEMBER_ID, SESSION_ID, CourseRole.MANAGER);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Success] hasAnyRoleForSession으로 여러 역할 중 하나를 확인한다")
		void test03() {
			SessionEntity entity = createSessionEntityWithCourseId();
			List<CourseRole> roles = List.of(CourseRole.MANAGER, CourseRole.MENTOR);
			when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(entity));
			when(courseAuthorizationPort.hasAnyRole(MEMBER_ID, COURSE_ID, roles)).thenReturn(true);

			boolean result = adapter.hasAnyRoleForSession(MEMBER_ID, SESSION_ID, roles);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] hasAnyRoleForSession으로 세션이 존재하지 않으면 false 반환")
		void test04() {
			when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

			boolean result = adapter.hasAnyRoleForSession(MEMBER_ID, SESSION_ID, List.of(CourseRole.MANAGER));

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Success] isMemberOfSession으로 세션 멤버 여부를 확인한다")
		void test05() {
			SessionEntity entity = createSessionEntityWithCourseId();
			when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(entity));
			when(courseAuthorizationPort.isMember(MEMBER_ID, COURSE_ID)).thenReturn(true);

			boolean result = adapter.isMemberOfSession(MEMBER_ID, SESSION_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] isMemberOfSession으로 세션이 존재하지 않으면 false 반환")
		void test06() {
			when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

			boolean result = adapter.isMemberOfSession(MEMBER_ID, SESSION_ID);

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("과정 ID 직접 전달 메서드")
	class CourseIdDirectTests {

		@Test
		@DisplayName("[Success] hasRoleForCourse로 과정에 대한 역할을 확인한다")
		void test01() {
			when(courseAuthorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER)).thenReturn(true);

			boolean result = adapter.hasRoleForCourse(MEMBER_ID, COURSE_ID, CourseRole.MANAGER);

			assertThat(result).isTrue();
			verify(courseAuthorizationPort).hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER);
		}

		@Test
		@DisplayName("[Success] hasAnyRoleForCourse로 여러 역할 중 하나를 확인한다")
		void test02() {
			List<CourseRole> roles = List.of(CourseRole.MANAGER, CourseRole.MENTOR);
			when(courseAuthorizationPort.hasAnyRole(MEMBER_ID, COURSE_ID, roles)).thenReturn(true);

			boolean result = adapter.hasAnyRoleForCourse(MEMBER_ID, COURSE_ID, roles);

			assertThat(result).isTrue();
			verify(courseAuthorizationPort).hasAnyRole(MEMBER_ID, COURSE_ID, roles);
		}

		@Test
		@DisplayName("[Success] isMemberOfCourse로 과정 멤버 여부를 확인한다")
		void test03() {
			when(courseAuthorizationPort.isMember(MEMBER_ID, COURSE_ID)).thenReturn(true);

			boolean result = adapter.isMemberOfCourse(MEMBER_ID, COURSE_ID);

			assertThat(result).isTrue();
			verify(courseAuthorizationPort).isMember(MEMBER_ID, COURSE_ID);
		}
	}
}

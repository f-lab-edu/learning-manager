package me.chan99k.learningmanager.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.CourseRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseSecurityService 테스트")
class CourseSecurityServiceTest {

	private static final Long COURSE_ID = 1L;
	private static final Long MEMBER_ID = 100L;

	@InjectMocks
	private CourseSecurityService courseSecurityService;

	@Mock
	private CourseAuthorizationPort authorizationPort;

	@Test
	@DisplayName("[Success] MANAGER 권한이 있으면 true를 반환한다")
	void test01() {
		when(authorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER)).thenReturn(true);

		boolean result = courseSecurityService.isManager(COURSE_ID, MEMBER_ID);

		assertThat(result).isTrue();
		verify(authorizationPort).hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER);
	}

	@Test
	@DisplayName("[Failure] MANAGER 권한이 없으면 false를 반환한다")
	void test02() {
		when(authorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER)).thenReturn(false);

		boolean result = courseSecurityService.isManager(COURSE_ID, MEMBER_ID);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("[Success] MANAGER 또는 MENTOR 권한이 있으면 true를 반환한다")
	void test03() {
		when(authorizationPort.hasAnyRole(MEMBER_ID, COURSE_ID, List.of(CourseRole.MANAGER, CourseRole.MENTOR)))
			.thenReturn(true);

		boolean result = courseSecurityService.isManagerOrMentor(COURSE_ID, MEMBER_ID);

		assertThat(result).isTrue();
		verify(authorizationPort).hasAnyRole(MEMBER_ID, COURSE_ID, List.of(CourseRole.MANAGER, CourseRole.MENTOR));
	}

	@Test
	@DisplayName("[Failure] MANAGER/MENTOR 권한이 없으면 false를 반환한다")
	void test04() {
		when(authorizationPort.hasAnyRole(MEMBER_ID, COURSE_ID, List.of(CourseRole.MANAGER, CourseRole.MENTOR)))
			.thenReturn(false);

		boolean result = courseSecurityService.isManagerOrMentor(COURSE_ID, MEMBER_ID);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("[Success] 과정 멤버이면 true를 반환한다")
	void test05() {
		when(authorizationPort.isMember(MEMBER_ID, COURSE_ID)).thenReturn(true);

		boolean result = courseSecurityService.isMember(COURSE_ID, MEMBER_ID);

		assertThat(result).isTrue();
		verify(authorizationPort).isMember(MEMBER_ID, COURSE_ID);
	}

	@Test
	@DisplayName("[Failure] 과정 멤버가 아니면 false를 반환한다")
	void test06() {
		when(authorizationPort.isMember(MEMBER_ID, COURSE_ID)).thenReturn(false);

		boolean result = courseSecurityService.isMember(COURSE_ID, MEMBER_ID);

		assertThat(result).isFalse();
	}
}

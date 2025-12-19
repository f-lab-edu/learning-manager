package me.chan99k.learningmanager.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.course.JpaCourseRepository;

@DisplayName("JpaCourseAuthorizationAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class JpaCourseAuthorizationAdapterTest {

	private static final Long MEMBER_ID = 1L;
	private static final Long COURSE_ID = 10L;
	@Mock
	private JpaCourseRepository courseRepository;
	private JpaCourseAuthorizationAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new JpaCourseAuthorizationAdapter(courseRepository);
	}

	@Test
	@DisplayName("[Success] hasRole로 특정 역할 보유 여부를 확인한다")
	void test01() {
		when(courseRepository.existsByMemberIdAndCourseIdAndRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
			.thenReturn(true);

		boolean result = adapter.hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("[Failure] hasRole로 역할이 없는 경우 false 반환")
	void test02() {
		when(courseRepository.existsByMemberIdAndCourseIdAndRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
			.thenReturn(false);

		boolean result = adapter.hasRole(MEMBER_ID, COURSE_ID, CourseRole.MANAGER);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("[Success] hasAnyRole로 여러 역할 중 하나를 보유하는지 확인한다")
	void test03() {
		List<CourseRole> roles = List.of(CourseRole.MANAGER, CourseRole.MENTOR);
		when(courseRepository.existsByMemberIdAndCourseIdAndRoleIn(MEMBER_ID, COURSE_ID, roles))
			.thenReturn(true);

		boolean result = adapter.hasAnyRole(MEMBER_ID, COURSE_ID, roles);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("[Success] isMember로 과정 멤버 여부를 확인한다")
	void test04() {
		when(courseRepository.existsByMemberIdAndCourseId(MEMBER_ID, COURSE_ID))
			.thenReturn(true);

		boolean result = adapter.isMember(MEMBER_ID, COURSE_ID);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("[Failure] isMember로 멤버가 아닌 경우 false 반환")
	void test05() {
		when(courseRepository.existsByMemberIdAndCourseId(MEMBER_ID, COURSE_ID))
			.thenReturn(false);

		boolean result = adapter.isMember(MEMBER_ID, COURSE_ID);

		assertThat(result).isFalse();
	}
}

package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.SystemRole;

@ExtendWith(MockitoExtension.class)
class CourseCreationServiceTest {

	@Mock
	private CourseCommandRepository commandRepository;

	@Mock
	private SystemAuthorizationPort systemAuthorizationPort;

	@InjectMocks
	private CourseCreationService courseCreationService;

	@Test
	@DisplayName("[Success] 관리자 권한으로 과정 생성에 성공한다")
	void test01() {
		// given
		Long adminId = 1L;
		when(systemAuthorizationPort.hasRole(adminId, SystemRole.ADMIN)).thenReturn(true);

		Course mockCourse = mock(Course.class);
		when(mockCourse.getId()).thenReturn(100L);
		when(commandRepository.create(any(Course.class))).thenReturn(mockCourse);

		CourseCreation.Request request = new CourseCreation.Request("Test Course", "Test Description");

		// when
		CourseCreation.Response response = courseCreationService.createCourse(adminId, request);

		// then
		assertThat(response.courseId()).isEqualTo(100L);
		verify(commandRepository).create(any(Course.class));
	}

	@Test
	@DisplayName("[Failure] 일반 회원 권한으로는 과정 생성에 실패한다")
	void test02() {
		// given
		Long memberId = 1L;
		when(systemAuthorizationPort.hasRole(memberId, SystemRole.ADMIN)).thenReturn(false);

		CourseCreation.Request request = new CourseCreation.Request("Test Course", "Test Description");

		// when & then
		assertThatThrownBy(() -> courseCreationService.createCourse(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.ADMIN_ONLY_COURSE_CREATION);
	}
}

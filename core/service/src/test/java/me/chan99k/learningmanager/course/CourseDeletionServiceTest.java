package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class CourseDeletionServiceTest {

	@InjectMocks
	private CourseDeletionService courseDeletionService;

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private CourseCommandRepository courseCommandRepository;

	@Mock
	private Course course;

	@Test
	@DisplayName("[Success] 과정 관리자가 과정 삭제에 성공한다")
	void test01() {
		long courseId = 1L;
		long managerId = 10L;
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		courseDeletionService.deleteCourse(managerId, courseId);

		verify(courseCommandRepository).delete(course);
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 DomainException이 발생한다")
	void test02() {
		long courseId = 1L;
		long nonManagerId = 11L;
		when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> courseDeletionService.deleteCourse(nonManagerId, courseId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		verify(courseCommandRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 과정 삭제 시 DomainException이 발생한다")
	void test03() {
		long courseId = 999L;
		long managerId = 10L;
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> courseDeletionService.deleteCourse(managerId, courseId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		verify(courseCommandRepository, never()).delete(any());
	}
}

package me.chan99k.learningmanager.application.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseCommandRepository;
import me.chan99k.learningmanager.course.CourseDeletionService;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.course.CourseQueryRepository;
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
	void deleteCourse_Success() {
		// given
		long courseId = 1L;
		long managerId = 10L;
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		courseDeletionService.deleteCourse(managerId, courseId);

		// then
		verify(courseCommandRepository).delete(course);
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 DomainException이 발생한다")
	void deleteCourse_Fail_NotManager() {
		// given
		long courseId = 1L;
		long nonManagerId = 11L;
		when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> courseDeletionService.deleteCourse(nonManagerId, courseId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		verify(courseCommandRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 과정 삭제 시 DomainException이 발생한다")
	void deleteCourse_Fail_CourseNotFound() {
		// given
		long courseId = 999L;
		long managerId = 10L;
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> courseDeletionService.deleteCourse(managerId, courseId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		verify(courseCommandRepository, never()).delete(any());
	}

	@Test
	@DisplayName("[Behavior] courseCommandRepository.delete()가 호출되는지 확인한다")
	void deleteCourse_VerifyRepositoryDelete() {
		// given
		long courseId = 1L;
		long managerId = 10L;
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		courseDeletionService.deleteCourse(managerId, courseId);

		// then
		verify(courseCommandRepository).delete(course);
	}
}

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

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseCommandRepository;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.course.Curriculum;
import me.chan99k.learningmanager.course.CurriculumDeletionService;
import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class CurriculumDeletionServiceTest {

	@InjectMocks
	private CurriculumDeletionService curriculumDeletionService;

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private CourseCommandRepository courseCommandRepository;

	@Mock
	private UserContext userContext;

	@Mock
	private Course course;

	@Mock
	private Curriculum curriculum;

	@Test
	@DisplayName("[Success] 과정 관리자가 커리큘럼 삭제에 성공한다")
	void deleteCurriculum_Success() {
		// given
		long courseId = 1L;
		long curriculumId = 10L;
		long managerId = 100L;
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);

		// when
		curriculumDeletionService.deleteCurriculum(courseId, curriculumId);

		// then
		verify(course).findCurriculumById(curriculumId);
		verify(course).removeCurriculum(curriculum);
		verify(courseCommandRepository).save(course);
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 DomainException이 발생한다")
	void deleteCurriculum_Fail_NotManager() {
		// given
		long courseId = 1L;
		long curriculumId = 10L;
		long nonManagerId = 101L;
		when(userContext.getCurrentMemberId()).thenReturn(nonManagerId);
		when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> curriculumDeletionService.deleteCurriculum(courseId, curriculumId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);

		verify(course, never()).findCurriculumById(anyLong());
		verify(course, never()).removeCurriculum(any());
		verify(courseCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 IllegalStateException이 발생한다")
	void deleteCurriculum_Fail_Unauthenticated() {
		// given
		long courseId = 1L;
		long curriculumId = 10L;
		when(userContext.getCurrentMemberId()).thenThrow(
			new IllegalStateException("인증된 사용자의 컨텍스트를 찾을 수 없습니다"));

		// when & then
		assertThatThrownBy(() -> curriculumDeletionService.deleteCurriculum(courseId, curriculumId))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("인증된 사용자의 컨텍스트를 찾을 수 없습니다");

		verify(courseQueryRepository, never()).findManagedCourseById(anyLong(), anyLong());
		verify(course, never()).findCurriculumById(anyLong());
		verify(course, never()).removeCurriculum(any());
		verify(courseCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 커리큘럼 삭제 시 IllegalArgumentException이 발생한다")
	void deleteCurriculum_Fail_CurriculumNotFound() {
		// given
		long courseId = 1L;
		long curriculumId = 999L;
		long managerId = 100L;
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId))
			.thenThrow(new IllegalArgumentException("해당 과정에 존재하지 않는 커리큘럼입니다. ID: " + curriculumId));

		// when & then
		assertThatThrownBy(() -> curriculumDeletionService.deleteCurriculum(courseId, curriculumId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("해당 과정에 존재하지 않는 커리큘럼입니다");

		verify(course).findCurriculumById(curriculumId);
		verify(course, never()).removeCurriculum(any());
		verify(courseCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Behavior] courseCommandRepository.save()가 호출되는지 확인한다")
	void deleteCurriculum_VerifyRepositorySave() {
		// given
		long courseId = 1L;
		long curriculumId = 10L;
		long managerId = 100L;
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);

		// when
		curriculumDeletionService.deleteCurriculum(courseId, curriculumId);

		// then
		verify(courseCommandRepository).save(course);
	}
}
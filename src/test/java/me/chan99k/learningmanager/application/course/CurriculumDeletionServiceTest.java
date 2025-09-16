package me.chan99k.learningmanager.application.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;

@ExtendWith(MockitoExtension.class)
class CurriculumDeletionServiceTest {

	@InjectMocks
	private CurriculumDeletionService curriculumDeletionService;

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private CourseCommandRepository courseCommandRepository;

	@Mock
	private Course course;

	@Mock
	private Curriculum curriculum;

	@Test
	@DisplayName("[Success] 과정 관리자가 커리큘럼 삭제에 성공한다")
	void deleteCurriculum_Success() {
		long courseId = 1L;
		long curriculumId = 10L;
		long managerId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);

			curriculumDeletionService.deleteCurriculum(courseId, curriculumId);

			verify(course).findCurriculumById(curriculumId);
			verify(course).removeCurriculum(curriculum);
			verify(courseCommandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 AuthorizationException이 발생한다")
	void deleteCurriculum_Fail_NotManager() {
		long courseId = 1L;
		long curriculumId = 10L;
		long nonManagerId = 101L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(nonManagerId));
			when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> curriculumDeletionService.deleteCurriculum(courseId, curriculumId))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			verify(course, never()).findCurriculumById(anyLong());
			verify(course, never()).removeCurriculum(any());
			verify(courseCommandRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 AuthenticationException이 발생한다")
	void deleteCurriculum_Fail_Unauthenticated() {
		long courseId = 1L;
		long curriculumId = 10L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			assertThatThrownBy(() -> curriculumDeletionService.deleteCurriculum(courseId, curriculumId))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);

			verify(courseQueryRepository, never()).findManagedCourseById(anyLong(), anyLong());
			verify(course, never()).findCurriculumById(anyLong());
			verify(course, never()).removeCurriculum(any());
			verify(courseCommandRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 커리큘럼 삭제 시 IllegalArgumentException이 발생한다")
	void deleteCurriculum_Fail_CurriculumNotFound() {
		long courseId = 1L;
		long curriculumId = 999L;
		long managerId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(course.findCurriculumById(curriculumId))
				.thenThrow(new IllegalArgumentException("해당 과정에 존재하지 않는 커리큘럼입니다. ID: " + curriculumId));

			assertThatThrownBy(() -> curriculumDeletionService.deleteCurriculum(courseId, curriculumId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("해당 과정에 존재하지 않는 커리큘럼입니다");

			verify(course).findCurriculumById(curriculumId);
			verify(course, never()).removeCurriculum(any());
			verify(courseCommandRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("[Behavior] courseCommandRepository.save()가 호출되는지 확인한다")
	void deleteCurriculum_VerifyRepositorySave() {
		long courseId = 1L;
		long curriculumId = 10L;
		long managerId = 100L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);

			curriculumDeletionService.deleteCurriculum(courseId, curriculumId);

			verify(courseCommandRepository).save(course);
		}
	}
}
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
		long courseId = 1L;
		long managerId = 10L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			courseDeletionService.deleteCourse(courseId);

			verify(courseCommandRepository).delete(course);
		}
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 AuthorizationException이 발생한다")
	void deleteCourse_Fail_NotManager() {
		long courseId = 1L;
		long nonManagerId = 11L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(nonManagerId));
			when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> courseDeletionService.deleteCourse(courseId))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			verify(courseCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 AuthenticationException이 발생한다")
	void deleteCourse_Fail_Unauthenticated() {
		long courseId = 1L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			assertThatThrownBy(() -> courseDeletionService.deleteCourse(courseId))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);

			verify(courseCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 과정 삭제 시 AuthorizationException이 발생한다")
	void deleteCourse_Fail_CourseNotFound() {
		long courseId = 999L;
		long managerId = 10L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> courseDeletionService.deleteCourse(courseId))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			verify(courseCommandRepository, never()).delete(any());
		}
	}

	@Test
	@DisplayName("[Behavior] courseCommandRepository.delete()가 호출되는지 확인한다")
	void deleteCourse_VerifyRepositoryDelete() {
		long courseId = 1L;
		long managerId = 10L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			courseDeletionService.deleteCourse(courseId);

			verify(courseCommandRepository).delete(course);
		}
	}
}
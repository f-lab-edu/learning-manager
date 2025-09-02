package me.chan99k.learningmanager.application.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CourseInfoUpdate;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;

@ExtendWith(MockitoExtension.class)
class CourseInfoUpdateServiceTest {

	private final Long courseId = 1L;
	private final Long managerId = 20L;
	private final String newTitle = "Updated Title";
	private final String newDescription = "Updated Description";

	private CourseInfoUpdateService service;
	@Mock
	private CourseQueryRepository queryRepository;
	@Mock
	private CourseCommandRepository commandRepository;
	@Mock
	private Course course;

	@BeforeEach
	void setUp() {
		service = new CourseInfoUpdateService(queryRepository, commandRepository);
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 과정 정보를 성공적으로 수정한다")
	void updateCourseInfo_Success() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, newDescription);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			// when
			service.updateCourseInfo(courseId, request);

			// then
			verify(course).updateTitle(newTitle);
			verify(course).updateDescription(newDescription);
			verify(commandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Success] 제목만 수정하는 경우 제목만 업데이트된다")
	void updateCourseInfo_TitleOnly() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, null);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			// when
			service.updateCourseInfo(courseId, request);

			// then
			verify(course).updateTitle(newTitle);
			verify(course, never()).updateDescription(any());
			verify(commandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Success] 설명만 수정하는 경우 설명만 업데이트된다")
	void updateCourseInfo_DescriptionOnly() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(null, newDescription);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

			// when
			service.updateCourseInfo(courseId, request);

			// then
			verify(course, never()).updateTitle(any());
			verify(course).updateDescription(newDescription);
			verify(commandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Failure] 인증된 사용자 정보가 없으면 AuthenticationException이 발생한다")
	void updateCourseInfo_Fail_Unauthenticated() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, newDescription);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> service.updateCourseInfo(courseId, request))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 과정이 존재하지 않거나 매니저가 아니면 AuthorizationException이 발생한다")
	void updateCourseInfo_Fail_CourseNotFoundOrNotManager() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, newDescription);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> service.updateCourseInfo(courseId, request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("[Failure] 제목과 설명이 모두 null이면 IllegalArgumentException이 발생한다")
	void updateCourseInfo_Fail_BothFieldsNull() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(null, null);

		// when & then
		assertThatThrownBy(() -> service.updateCourseInfo(courseId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("제목 또는 설명 중 하나 이상을 입력해주세요");
	}

	@Test
	@DisplayName("[Failure] 제목이 유효하지 않으면 도메인 예외가 발생한다")
	void updateCourseInfo_Fail_InvalidTitle() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request("", newDescription);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			doThrow(new IllegalArgumentException("과정 제목은 필수입니다"))
				.when(course).updateTitle("");

			// when & then
			assertThatThrownBy(() -> service.updateCourseInfo(courseId, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("과정 제목은 필수입니다");
		}
	}

	@Test
	@DisplayName("[Failure] 설명이 유효하지 않으면 도메인 예외가 발생한다")
	void updateCourseInfo_Fail_InvalidDescription() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, "");

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			doThrow(new IllegalArgumentException("과정 설명은 필수입니다"))
				.when(course).updateDescription("");

			// when & then
			assertThatThrownBy(() -> service.updateCourseInfo(courseId, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("과정 설명은 필수입니다");
		}
	}
}
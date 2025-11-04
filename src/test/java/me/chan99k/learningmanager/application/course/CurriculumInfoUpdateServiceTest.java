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
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.course.provides.CurriculumInfoUpdate;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;

@ExtendWith(MockitoExtension.class)
class CurriculumInfoUpdateServiceTest {

	private final Long courseId = 1L;
	private final Long curriculumId = 10L;
	private final Long managerId = 20L;
	private final String newTitle = "Updated Title";
	private final String newDescription = "Updated Description";

	private CurriculumInfoUpdateService service;
	@Mock
	private CourseQueryRepository queryRepository;
	@Mock
	private CourseCommandRepository commandRepository;
	@Mock
	private UserContext userContext;
	@Mock
	private Course course;
	@Mock
	private Curriculum curriculum;

	@BeforeEach
	void setUp() {
		service = new CurriculumInfoUpdateService(queryRepository, commandRepository, userContext);
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 커리큘럼 정보를 성공적으로 수정한다")
	void updateCurriculumInfo_Success() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(newTitle, newDescription);
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);

		// when
		service.updateCurriculumInfo(courseId, curriculumId, request);

		// then
		verify(curriculum).updateTitle(newTitle);
		verify(curriculum).updateDescription(newDescription);
		verify(commandRepository).save(course);
	}

	@Test
	@DisplayName("[Success] 제목만 수정하는 경우 제목만 업데이트된다")
	void updateCurriculumInfo_TitleOnly() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(newTitle, null);
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);

		// when
		service.updateCurriculumInfo(courseId, curriculumId, request);

		// then
		verify(curriculum).updateTitle(newTitle);
		verify(curriculum, never()).updateDescription(any());
		verify(commandRepository).save(course);
	}

	@Test
	@DisplayName("[Success] 설명만 수정하는 경우 설명만 업데이트된다")
	void updateCurriculumInfo_DescriptionOnly() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(null, newDescription);
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);

		// when
		service.updateCurriculumInfo(courseId, curriculumId, request);

		// then
		verify(curriculum, never()).updateTitle(any());
		verify(curriculum).updateDescription(newDescription);
		verify(commandRepository).save(course);
	}

	@Test
	@DisplayName("[Failure] 인증된 사용자 정보가 없으면 AuthenticationException이 발생한다")
	void updateCurriculumInfo_Fail_Unauthenticated() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(newTitle, newDescription);
		when(userContext.getCurrentMemberId()).thenThrow(
			new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> service.updateCurriculumInfo(courseId, curriculumId, request))
			.isInstanceOf(AuthenticationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
	}

	@Test
	@DisplayName("[Failure] 과정이 존재하지 않거나 매니저가 아니면 AuthorizationException이 발생한다")
	void updateCurriculumInfo_Fail_CourseNotFoundOrNotManager() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(newTitle, newDescription);
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> service.updateCurriculumInfo(courseId, curriculumId, request))
			.isInstanceOf(AuthorizationException.class)
			.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
	}

	@Test
	@DisplayName("[Failure] 커리큘럼이 과정에 존재하지 않으면 IllegalArgumentException이 발생한다")
	void updateCurriculumInfo_Fail_CurriculumNotFound() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(newTitle, newDescription);
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId))
			.thenThrow(new IllegalArgumentException("커리큘럼을 찾을 수 없습니다"));

		// when & then
		assertThatThrownBy(() -> service.updateCurriculumInfo(courseId, curriculumId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("커리큘럼을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("[Failure] 제목과 설명이 모두 null이면 IllegalArgumentException이 발생한다")
	void updateCurriculumInfo_Fail_BothFieldsNull() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(null, null);

		// when & then
		assertThatThrownBy(() -> service.updateCurriculumInfo(courseId, curriculumId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("제목 또는 설명 중 하나 이상을 입력해주세요");
	}

	@Test
	@DisplayName("[Failure] 제목이 유효하지 않으면 도메인 예외가 발생한다")
	void updateCurriculumInfo_Fail_InvalidTitle() {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request("", newDescription);
		when(userContext.getCurrentMemberId()).thenReturn(managerId);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		when(course.findCurriculumById(curriculumId)).thenReturn(curriculum);
		doThrow(new IllegalArgumentException("커리큘럼 제목은 필수입니다"))
			.when(curriculum).updateTitle("");

		// when & then
		assertThatThrownBy(() -> service.updateCurriculumInfo(courseId, curriculumId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("커리큘럼 제목은 필수입니다");
	}
}
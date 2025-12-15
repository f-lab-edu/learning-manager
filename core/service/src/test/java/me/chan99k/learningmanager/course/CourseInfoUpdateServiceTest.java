package me.chan99k.learningmanager.course;

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

import me.chan99k.learningmanager.exception.DomainException;

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
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		service.updateCourseInfo(managerId, courseId, request);

		// then
		verify(course).updateTitle(newTitle);
		verify(course).updateDescription(newDescription);
		verify(commandRepository).save(course);
	}

	@Test
	@DisplayName("[Success] 제목만 수정하는 경우 제목만 업데이트된다")
	void updateCourseInfo_TitleOnly() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, null);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		service.updateCourseInfo(managerId, courseId, request);

		// then
		verify(course).updateTitle(newTitle);
		verify(course, never()).updateDescription(any());
		verify(commandRepository).save(course);
	}

	@Test
	@DisplayName("[Success] 설명만 수정하는 경우 설명만 업데이트된다")
	void updateCourseInfo_DescriptionOnly() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(null, newDescription);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));

		// when
		service.updateCourseInfo(managerId, courseId, request);

		// then
		verify(course, never()).updateTitle(any());
		verify(course).updateDescription(newDescription);
		verify(commandRepository).save(course);
	}

	@Test
	@DisplayName("[Failure] 과정이 존재하지 않거나 매니저가 아니면 DomainException이 발생한다")
	void updateCourseInfo_Fail_CourseNotFoundOrNotManager() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, newDescription);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> service.updateCourseInfo(managerId, courseId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.NOT_COURSE_MANAGER);
	}

	@Test
	@DisplayName("[Failure] 제목과 설명이 모두 null이면 IllegalArgumentException이 발생한다")
	void updateCourseInfo_Fail_BothFieldsNull() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(null, null);

		// when & then
		assertThatThrownBy(() -> service.updateCourseInfo(managerId, courseId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("제목 또는 설명 중 하나 이상을 입력해주세요");
	}

	@Test
	@DisplayName("[Failure] 제목이 유효하지 않으면 도메인 예외가 발생한다")
	void updateCourseInfo_Fail_InvalidTitle() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request("", newDescription);
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		doThrow(new IllegalArgumentException("과정 제목은 필수입니다"))
			.when(course).updateTitle("");

		// when & then
		assertThatThrownBy(() -> service.updateCourseInfo(managerId, courseId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("과정 제목은 필수입니다");
	}

	@Test
	@DisplayName("[Failure] 설명이 유효하지 않으면 도메인 예외가 발생한다")
	void updateCourseInfo_Fail_InvalidDescription() {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(newTitle, "");
		when(queryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
		doThrow(new IllegalArgumentException("과정 설명은 필수입니다"))
			.when(course).updateDescription("");

		// when & then
		assertThatThrownBy(() -> service.updateCourseInfo(managerId, courseId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("과정 설명은 필수입니다");
	}
}

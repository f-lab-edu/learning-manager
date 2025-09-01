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
import me.chan99k.learningmanager.application.course.provides.CurriculumCreation;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {

	@InjectMocks
	private CurriculumCreationService curriculumService;

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private CourseCommandRepository courseCommandRepository;

	@Mock
	private Course course;

	@Test
	@DisplayName("[Success] 과정 관리자가 커리큘럼 생성에 성공한다")
	void createCurriculum_Success() {
		long courseId = 1L;
		long managerId = 10L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", "JPA의 기본 개념을 학습합니다.");

		Curriculum newCurriculum = mock(Curriculum.class);
		when(newCurriculum.getId()).thenReturn(101L);
		when(newCurriculum.getTitle()).thenReturn(request.title());

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(course.addCurriculum(request.title(), request.description())).thenReturn(newCurriculum);

			CurriculumCreation.Response response = curriculumService.createCurriculum(courseId, request);

			assertThat(response).isNotNull();
			assertThat(response.curriculumId()).isEqualTo(101L);
			assertThat(response.title()).isEqualTo("JPA 기초");
		}
	}

	@Test
	@DisplayName("[Failure] 과정 관리자가 아니면 AuthorizationException이 발생한다")
	void createCurriculum_Fail_NotManager() {
		long courseId = 1L;
		long nonManagerId = 11L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", null);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(nonManagerId));
			when(courseQueryRepository.findManagedCourseById(courseId, nonManagerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> curriculumService.createCurriculum(courseId, request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 AuthenticationException이 발생한다")
	void createCurriculum_Fail_Unauthenticated() {
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", null);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			assertThatThrownBy(() -> curriculumService.createCurriculum(courseId, request))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Success] 설명이 null인 커리큘럼도 생성에 성공한다")
	void createCurriculum_Success_NullDescription() {
		long courseId = 1L;
		long managerId = 10L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("Spring Security", null);

		Curriculum newCurriculum = mock(Curriculum.class);
		when(newCurriculum.getId()).thenReturn(102L);
		when(newCurriculum.getTitle()).thenReturn(request.title());

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(course.addCurriculum(request.title(), request.description())).thenReturn(newCurriculum);

			CurriculumCreation.Response response = curriculumService.createCurriculum(courseId, request);

			assertThat(response).isNotNull();
			assertThat(response.curriculumId()).isEqualTo(102L);
			assertThat(response.title()).isEqualTo("Spring Security");
		}
	}

	@Test
	@DisplayName("[Success] 빈 문자열 설명도 허용한다")
	void createCurriculum_Success_EmptyDescription() {
		long courseId = 1L;
		long managerId = 10L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("React 기초", "");

		Curriculum newCurriculum = mock(Curriculum.class);
		when(newCurriculum.getId()).thenReturn(103L);
		when(newCurriculum.getTitle()).thenReturn(request.title());


		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(course.addCurriculum(request.title(), request.description())).thenReturn(newCurriculum);

			CurriculumCreation.Response response = curriculumService.createCurriculum(courseId, request);

			assertThat(response).isNotNull();
			assertThat(response.curriculumId()).isEqualTo(103L);
			assertThat(response.title()).isEqualTo("React 기초");
		}
	}

	@Test
	@DisplayName("[Behavior] courseCommandRepository.save()가 호출되는지 확인한다")
	void createCurriculum_VerifyRepositorySave() {
		long courseId = 1L;
		long managerId = 10L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("Docker 기초", "컨테이너 기술 학습");

		Curriculum newCurriculum = Curriculum.create(course, request.title(), request.description());

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findManagedCourseById(courseId, managerId)).thenReturn(Optional.of(course));
			when(course.addCurriculum(request.title(), request.description())).thenReturn(newCurriculum);

			curriculumService.createCurriculum(courseId, request);

			verify(courseCommandRepository).save(course);
		}
	}
}

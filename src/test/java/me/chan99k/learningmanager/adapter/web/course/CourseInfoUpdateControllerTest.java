package me.chan99k.learningmanager.adapter.web.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.course.provides.CourseInfoUpdate;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;

@WebMvcTest(controllers = CourseInfoUpdateController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class CourseInfoUpdateControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CourseInfoUpdate courseInfoUpdate;

	@MockBean
	private me.chan99k.learningmanager.application.UserContext userContext;

	@MockBean(name = "courseTaskExecutor")
	private Executor courseTaskExecutor;

	@BeforeEach
	void setUp() {
		// 비동기 작업을 동기적으로 실행하도록 설정
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(courseTaskExecutor).execute(any(Runnable.class));

		// 모든 테스트에서 기본적으로 인증된 사용자가 있도록 설정
		given(userContext.getCurrentMemberId()).willReturn(1L);
		given(userContext.isAuthenticated()).willReturn(true);
	}


	@Test
	@DisplayName("[Success] 과정 정보 수정 요청이 성공하면 200 OK를 반환한다")
	void updateCourse_Success() throws Exception {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request("Updated Title", "Updated Description");
		doNothing().when(courseInfoUpdate)
			.updateCourseInfo(anyLong(), any(CourseInfoUpdate.Request.class));

		// when & then
		mockMvc.perform(put("/api/v1/courses/{courseId}", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		verify(courseInfoUpdate).updateCourseInfo(1L, request);
	}

	@Test
	@DisplayName("[Success] 제목만 수정 요청이 성공하면 200 OK를 반환한다")
	void updateCourse_TitleOnly_Success() throws Exception {
		// given
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request("Updated Title", null);
		doNothing().when(courseInfoUpdate)
			.updateCourseInfo(anyLong(), any(CourseInfoUpdate.Request.class));

		// when & then
		mockMvc.perform(put("/api/v1/courses/{courseId}", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[Success] 설명만 수정 요청이 성공하면 200 OK를 반환한다")
	void updateCourse_DescriptionOnly_Success() throws Exception {
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(null, "Updated Description");
		doNothing().when(courseInfoUpdate)
			.updateCourseInfo(anyLong(), any(CourseInfoUpdate.Request.class));

		mockMvc.perform(put("/api/v1/courses/{courseId}", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[Failure] 제목과 설명이 모두 null이면 400 Bad Request를 반환한다")
	void updateCourse_Fail_BothFieldsNull() throws Exception {
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request(null, null);
		doThrow(new IllegalArgumentException("제목 또는 설명 중 하나 이상을 입력해주세요"))
			.when(courseInfoUpdate)
			.updateCourseInfo(anyLong(), any(CourseInfoUpdate.Request.class));

		mockMvc.perform(put("/api/v1/courses/{courseId}", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 인증 정보가 없으면 403 Forbidden를 반환한다")
	void updateCourse_Fail_Unauthenticated() throws Exception {
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request("Updated Title", "Updated Description");
		doThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND))
			.when(courseInfoUpdate)
			.updateCourseInfo(anyLong(), any(CourseInfoUpdate.Request.class));

		mockMvc.perform(put("/api/v1/courses/{courseId}", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));
	}

	@Test
	@DisplayName("[Failure] 권한이 없으면 403 Forbidden을 반환한다")
	void updateCourse_Fail_Authorization() throws Exception {
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request("Updated Title", "Updated Description");
		doThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED))
			.when(courseInfoUpdate)
			.updateCourseInfo(anyLong(), any(CourseInfoUpdate.Request.class));

		mockMvc.perform(put("/api/v1/courses/{courseId}", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHORIZATION_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("[Failure] 유효하지 않은 제목이면 400 Bad Request를 반환한다")
	void updateCourse_Fail_InvalidTitle() throws Exception {
		CourseInfoUpdate.Request request = new CourseInfoUpdate.Request("", "Updated Description");
		doThrow(new IllegalArgumentException("과정 제목은 필수입니다"))
			.when(courseInfoUpdate)
			.updateCourseInfo(anyLong(), any(CourseInfoUpdate.Request.class));

		mockMvc.perform(put("/api/v1/courses/{courseId}", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}
}
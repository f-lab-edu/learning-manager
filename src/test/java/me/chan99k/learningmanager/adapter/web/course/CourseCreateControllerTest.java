package me.chan99k.learningmanager.adapter.web.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
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

import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.course.provides.CourseCreation;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;

@WebMvcTest(controllers = CourseCreateController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import({
	GlobalExceptionHandler.class
})
class CourseCreateControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	CourseCreation courseCreation;

	@MockBean
	me.chan99k.learningmanager.application.course.CourseCreationService courseCreationService;

	@MockBean
	UserContext userContext;

	@MockBean(name = "memberTaskExecutor")
	Executor memberTaskExecutor;


	@BeforeEach
	void setUp() {
		// Executor가 동기적으로 실행하도록 설정
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run(); // 즉시 실행
			return null;
		}).given(memberTaskExecutor).execute(any(Runnable.class));

		// UserContext 모킹
		given(userContext.getCurrentMemberId()).willReturn(1L);
		given(userContext.isAuthenticated()).willReturn(true);
	}


	@Test
	@DisplayName("[Success] 유효한 요청으로 과정 생성에 성공한다")
	void createCourse_success() throws Exception {
		// given
		CourseCreation.Request request = new CourseCreation.Request("Spring Boot 스터디", "Spring Boot 심화 과정");
		CourseCreation.Response response = new CourseCreation.Response(100L);

		given(courseCreationService.createCourse(any(CourseCreation.Request.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.courseId").value(100L));
	}

	@Test
	@DisplayName("[Failure] 제목이 누락된 경우 400 Bad Request를 반환한다")
	void createCourse_failure_missing_title() throws Exception {
		// given
		CourseCreation.Request request = new CourseCreation.Request(null, "Spring Boot 심화 과정");

		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
			.andExpect(jsonPath("$.title").value("Validation Error"))
			.andExpect(jsonPath("$.detail").exists());
	}

	@Test
	@DisplayName("[Failure] 빈 제목인 경우 400 Bad Request를 반환한다")
	void createCourse_failure_blank_title() throws Exception {
		// given
		CourseCreation.Request request = new CourseCreation.Request("", "Spring Boot 심화 과정");

		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
			.andExpect(jsonPath("$.title").value("Validation Error"));
	}

	@Test
	@DisplayName("[Failure] 권한 없는 사용자가 과정 생성 시도 시 403 Forbidden을 반환한다")
	void createCourse_failure_insufficient_permission() throws Exception {
		// given
		CourseCreation.Request request = new CourseCreation.Request("Spring Boot 스터디", "Spring Boot 심화 과정");

		given(courseCreationService.createCourse(any(CourseCreation.Request.class)))
			.willThrow(
				new AuthorizationException(
					me.chan99k.learningmanager.adapter.auth.AuthProblemCode.AUTHORIZATION_REQUIRED));

		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isForbidden())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자가 과정 생성 시도 시 403 Forbidden를 반환한다")
	void createCourse_failure_unauthenticated() throws Exception {
		// given
		CourseCreation.Request request = new CourseCreation.Request("Spring Boot 스터디", "Spring Boot 심화 과정");

		given(courseCreationService.createCourse(any(CourseCreation.Request.class)))
			.willThrow(new AuthenticationException(
				me.chan99k.learningmanager.adapter.auth.AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		mockMvc.perform(post("/api/v1/courses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));
	}

	@Test
	@DisplayName("[Failure] 잘못된 JSON 형식인 경우 400 Bad Request를 반환한다")
	void createCourse_failure_invalid_json() throws Exception {
		// given
		String invalidJson = "{\"title\": \"Spring Boot 스터디\", \"description\":}";

		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}
}
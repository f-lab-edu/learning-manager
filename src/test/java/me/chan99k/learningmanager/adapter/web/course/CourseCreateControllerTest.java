package me.chan99k.learningmanager.adapter.web.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.adapter.auth.AccessTokenProvider;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.adapter.auth.BcryptPasswordEncoder;
import me.chan99k.learningmanager.adapter.auth.JwtCredentialProvider;
import me.chan99k.learningmanager.adapter.auth.jwt.AccessJwtTokenProvider;
import me.chan99k.learningmanager.adapter.auth.jwt.InMemoryJwtTokenRevocationProvider;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.course.provides.CourseCreation;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;

@WebMvcTest(controllers = CourseCreateController.class)
@Import({
	GlobalExceptionHandler.class,
	JwtCredentialProvider.class,
	AccessJwtTokenProvider.class,
	InMemoryJwtTokenRevocationProvider.class,
	BcryptPasswordEncoder.class
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
	AccessTokenProvider<Long> accessTokenProvider;

	@MockBean(name = "memberTaskExecutor")
	Executor memberTaskExecutor;

	@MockBean(name = "courseTaskExecutor")
	AsyncTaskExecutor courseTaskExecutor;

	@BeforeEach
	void setUp() {
		// Executor가 동기적으로 실행하도록 설정
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run(); // 즉시 실행
			return null;
		}).given(memberTaskExecutor).execute(any(Runnable.class));

		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run(); // 즉시 실행
			return null;
		}).given(courseTaskExecutor).execute(any(Runnable.class));

		// 인증 컨텍스트 설정
		AuthenticationContextHolder.setCurrentMemberId(1L);

		// AccessTokenProvider 모킹
		given(accessTokenProvider.validateAccessToken("valid-token")).willReturn(true);
		given(accessTokenProvider.getIdFromAccessToken("valid-token")).willReturn(1L);
	}

	@AfterEach
	void tearDown() {
		AuthenticationContextHolder.clear();
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
		MvcResult mvcResult = mockMvc.perform(post("/api/v1/courses")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
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
		MvcResult mvcResult = mockMvc.perform(post("/api/v1/courses")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andDo(print())
			.andExpect(status().isForbidden())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자가 과정 생성 시도 시 401 Unauthorized를 반환한다")
	void createCourse_failure_unauthenticated() throws Exception {
		// given
		CourseCreation.Request request = new CourseCreation.Request("Spring Boot 스터디", "Spring Boot 심화 과정");

		given(courseCreation.createCourse(any(CourseCreation.Request.class)))
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
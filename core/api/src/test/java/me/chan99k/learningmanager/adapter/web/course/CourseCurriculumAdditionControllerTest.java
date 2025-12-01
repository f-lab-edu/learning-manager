package me.chan99k.learningmanager.adapter.web.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.application.auth.UserContext;
import me.chan99k.learningmanager.application.course.provides.CurriculumCreation;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthenticationException;
import me.chan99k.learningmanager.domain.exception.AuthorizationException;
import me.chan99k.learningmanager.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.web.course.CourseCurriculumAdditionController;

@WebMvcTest(controllers = CourseCurriculumAdditionController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class CourseCurriculumAdditionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CurriculumCreation curriculumCreation;

	@MockBean(name = "courseTaskExecutor")
	private AsyncTaskExecutor courseTaskExecutor;

	@MockBean
	private UserContext userContext;

	@BeforeEach
	void setUp() {
		// 비동기 작업을 테스트에서 동기적으로 실행하도록 설정
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(courseTaskExecutor).execute(any(Runnable.class));

		// 인증된 사용자 설정
		given(userContext.getCurrentMemberId()).willReturn(1L);
		given(userContext.isAuthenticated()).willReturn(true);
	}

	@Test
	@DisplayName("[Success] 유효한 요청으로 커리큘럼 생성 시 201 Created를 반환한다")
	void createCurriculum_Success() throws Exception {
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", "설명");
		CurriculumCreation.Response response = new CurriculumCreation.Response(101L, "JPA 기초");

		given(curriculumCreation.createCurriculum(eq(courseId), any(CurriculumCreation.Request.class)))
			.willReturn(response);

		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.curriculumId").value(101L))
			.andExpect(jsonPath("$.title").value("JPA 기초"));
	}

	@Test
	@DisplayName("[Failure] 제목이 비어있는 요청 시 400 Bad Request를 반환한다")
	void createCurriculum_Fail_BlankTitle() throws Exception {
		// given
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("", "설명");

		// when & then
		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Validation Error"))
			.andExpect(jsonPath("$.detail").value("커리큘럼 제목은 필수입니다"));
	}

	@Test
	@DisplayName("[Failure] 권한이 없는 사용자의 요청 시 403 Forbidden을 반환한다")
	void createCurriculum_Fail_Forbidden() throws Exception {
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", "설명");

		given(curriculumCreation.createCurriculum(eq(courseId), any(CurriculumCreation.Request.class)))
			.willThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHORIZATION_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("[Failure] 인증 헤더 없이 요청 시 403 Forbidden를 반환한다")
	void createCurriculum_Fail_Unauthorized() throws Exception {
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", "설명");

		// 서비스에서 인증 예외 발생하도록 설정
		given(curriculumCreation.createCurriculum(eq(courseId), any(CurriculumCreation.Request.class)))
			.willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// Authorization 헤더 없이 요청
		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("[Failure] 유효하지 않은 토큰으로 요청 시 401 Unauthorized를 반환한다")
	void createCurriculum_Fail_InvalidToken() throws Exception {
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", "설명");

		// 서비스에서 인증 예외 발생하도록 설정
		given(curriculumCreation.createCurriculum(eq(courseId), any(CurriculumCreation.Request.class)))
			.willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer invalid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));
	}

	@Test
	@DisplayName("[Success] null 설명으로 커리큘럼 생성 성공")
	void createCurriculum_Success_NullDescription() throws Exception {
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("Spring Security", null);
		CurriculumCreation.Response response = new CurriculumCreation.Response(102L, "Spring Security");

		given(curriculumCreation.createCurriculum(eq(courseId), any(CurriculumCreation.Request.class)))
			.willReturn(response);

		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.curriculumId").value(102L))
			.andExpect(jsonPath("$.title").value("Spring Security"));
	}

	@Test
	@DisplayName("[Success] 빈 문자열 설명으로 커리큘럼 생성 성공")
	void createCurriculum_Success_EmptyDescription() throws Exception {
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("React 기초", "");
		CurriculumCreation.Response response = new CurriculumCreation.Response(103L, "React 기초");

		given(curriculumCreation.createCurriculum(eq(courseId), any(CurriculumCreation.Request.class)))
			.willReturn(response);

		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.curriculumId").value(103L))
			.andExpect(jsonPath("$.title").value("React 기초"));
	}

	@Test
	@DisplayName("[Failure] 제목이 공백만 포함하는 요청 시 400 Bad Request를 반환한다")
	void createCurriculum_Fail_WhitespaceTitle() throws Exception {
		// given
		long courseId = 1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("   ", "설명");

		// when & then
		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Validation Error"))
			.andExpect(jsonPath("$.detail").value("커리큘럼 제목은 필수입니다"));
	}

	@Test
	@DisplayName("[Failure] 잘못된 JSON 형식으로 요청 시 400 Bad Request를 반환한다")
	void createCurriculum_Fail_InvalidJson() throws Exception {
		long courseId = 1L;

		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
						{
							"invalid": "json"
						}
						"""
				)
			)
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] courseId가 음수인 경우 404 Not Found를 반환한다")
	void createCurriculum_Fail_NegativeCourseId() throws Exception {
		long courseId = -1L;
		CurriculumCreation.Request request = new CurriculumCreation.Request("JPA 기초", "설명");

		given(curriculumCreation.createCurriculum(eq(courseId), any(CurriculumCreation.Request.class)))
			.willThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		mockMvc.perform(post("/api/v1/courses/{courseId}/curriculums", courseId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());
	}
}
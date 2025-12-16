package me.chan99k.learningmanager.controller.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.config.SecurityConfig;
import me.chan99k.learningmanager.course.CourseCreation;
import me.chan99k.learningmanager.filter.JwtAuthenticationFilter;
import me.chan99k.learningmanager.member.SystemRole;

@WebMvcTest(controllers = CourseCreateController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("CourseCreateController 테스트")
class CourseCreateControllerTest {

	private static final Long MEMBER_ID = 1L;
	private static final String MEMBER_EMAIL = "test@example.com";
	private static final String VALID_TOKEN = "valid-jwt-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CourseCreation courseCreation;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private SystemAuthorizationPort systemAuthorizationPort;

	@BeforeEach
	void setUp() {
		// 유효한 토큰에 대한 스터빙
		JwtProvider.Claims claims = new JwtProvider.Claims(
			MEMBER_ID,
			MEMBER_EMAIL,
			Instant.now().plusSeconds(3600)
		);

		given(jwtProvider.isValid(VALID_TOKEN)).willReturn(true);
		given(jwtProvider.validateAndGetClaims(VALID_TOKEN)).willReturn(claims);
		given(systemAuthorizationPort.getRoles(MEMBER_ID)).willReturn(Set.of(SystemRole.MEMBER));
	}

	@Nested
	@DisplayName("과정 생성 API (POST /api/v1/courses)")
	class CreateCourseTest {

		@Test
		@DisplayName("[Success] 유효한 요청으로 과정 생성 성공 - 201 Created")
		void create_course_success() throws Exception {
			CourseCreation.Request request = new CourseCreation.Request(
				"Spring Boot 기초",
				"스프링 부트 학습 과정"
			);
			CourseCreation.Response response = new CourseCreation.Response(100L);

			given(courseCreation.createCourse(eq(MEMBER_ID), any(CourseCreation.Request.class)))
				.willReturn(response);

			mockMvc.perform(post("/api/v1/courses")
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.courseId").value(100L));

			then(courseCreation).should().createCourse(eq(MEMBER_ID), any(CourseCreation.Request.class));
		}

		@Test
		@DisplayName("[Failure] 제목 누락 - 400 Bad Request")
		void create_course_without_title() throws Exception {
			String requestJson = "{\"description\":\"스프링 부트 학습 과정\"}";

			mockMvc.perform(post("/api/v1/courses")
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

			then(courseCreation).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 빈 제목 - 400 Bad Request")
		void create_course_with_blank_title() throws Exception {
			String requestJson = "{\"title\":\"\",\"description\":\"스프링 부트 학습 과정\"}";

			mockMvc.perform(post("/api/v1/courses")
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

			then(courseCreation).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void create_course_without_token() throws Exception {
			CourseCreation.Request request = new CourseCreation.Request(
				"Spring Boot 기초",
				"스프링 부트 학습 과정"
			);

			// Spring Security 기본 동작: 인증되지 않은 요청 → 403 Forbidden
			mockMvc.perform(post("/api/v1/courses")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(courseCreation).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 유효하지 않은 토큰 - 403 Forbidden")
		void create_course_with_invalid_token() throws Exception {
			String invalidToken = "invalid-token";
			given(jwtProvider.isValid(invalidToken)).willReturn(false);

			CourseCreation.Request request = new CourseCreation.Request(
				"Spring Boot 기초",
				"스프링 부트 학습 과정"
			);

			mockMvc.perform(post("/api/v1/courses")
					.header("Authorization", "Bearer " + invalidToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(courseCreation).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Success] 설명 없이 과정 생성 가능 - 201 Created")
		void create_course_without_description() throws Exception {
			CourseCreation.Request request = new CourseCreation.Request(
				"Spring Boot 기초",
				null
			);
			CourseCreation.Response response = new CourseCreation.Response(100L);

			given(courseCreation.createCourse(eq(MEMBER_ID), any(CourseCreation.Request.class)))
				.willReturn(response);

			mockMvc.perform(post("/api/v1/courses")
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.courseId").value(100L));
		}
	}
}

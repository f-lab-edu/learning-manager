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
import org.springframework.test.web.servlet.MockMvc;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.course.provides.CourseDeletion;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;

@WebMvcTest(controllers = CourseDeletionController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class CourseDeletionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CourseDeletion courseDeletion;

	@MockBean(name = "courseTaskExecutor")
	private AsyncTaskExecutor courseTaskExecutor;

	@MockBean
	private me.chan99k.learningmanager.application.UserContext userContext;

	@BeforeEach
	void setUp() {
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(courseTaskExecutor).execute(any(Runnable.class));

		given(userContext.getCurrentMemberId()).willReturn(1L);
		given(userContext.isAuthenticated()).willReturn(true);
	}

	@Test
	@DisplayName("[Success] 유효한 요청으로 과정 삭제 시 204 No Content를 반환한다")
	void deleteCourse_Success() throws Exception {
		long courseId = 1L;

		willDoNothing().given(courseDeletion).deleteCourse(courseId);

		mockMvc.perform(delete("/api/v1/courses/{courseId}", courseId)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("[Failure] 권한이 없는 사용자의 요청 시 403 Forbidden을 반환한다")
	void deleteCourse_Fail_Forbidden() throws Exception {
		long courseId = 1L;

		willThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED))
			.given(courseDeletion).deleteCourse(courseId);

		mockMvc.perform(delete("/api/v1/courses/{courseId}", courseId)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHORIZATION_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("[Failure] 인증 헤더 없이 요청 시 403 Forbidden를 반환한다")
	void deleteCourse_Fail_Unauthorized() throws Exception {
		long courseId = 1L;

		willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND))
			.given(courseDeletion).deleteCourse(courseId);

		mockMvc.perform(delete("/api/v1/courses/{courseId}", courseId))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("[Failure] 유효하지 않은 토큰으로 요청 시 401 Unauthorized를 반환한다")
	void deleteCourse_Fail_InvalidToken() throws Exception {
		long courseId = 1L;

		willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND))
			.given(courseDeletion).deleteCourse(courseId);

		mockMvc.perform(delete("/api/v1/courses/{courseId}", courseId)
				.header("Authorization", "Bearer invalid-token"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("[Failure] courseId가 음수인 경우 403 Forbidden을 반환한다")
	void deleteCourse_Fail_NegativeCourseId() throws Exception {
		long courseId = -1L;

		willThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED))
			.given(courseDeletion).deleteCourse(courseId);

		mockMvc.perform(delete("/api/v1/courses/{courseId}", courseId)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 과정 삭제 시 403 Forbidden을 반환한다")
	void deleteCourse_Fail_CourseNotFound() throws Exception {
		long courseId = 999L;

		willThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED))
			.given(courseDeletion).deleteCourse(courseId);

		mockMvc.perform(delete("/api/v1/courses/{courseId}", courseId)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHORIZATION_REQUIRED.getCode()));
	}
}
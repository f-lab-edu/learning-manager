package me.chan99k.learningmanager.adapter.web.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import me.chan99k.learningmanager.adapter.auth.AccessTokenProvider;
import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.course.provides.CurriculumDeletion;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;

@WebMvcTest(controllers = CurriculumDeletionController.class)
@Import(GlobalExceptionHandler.class)
class CurriculumDeletionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CurriculumDeletion curriculumDeletion;

	@MockBean
	private AccessTokenProvider<Long> accessTokenProvider;

	@BeforeEach
	void setUp() {
		// 토큰 검증 모킹
		when(accessTokenProvider.validateAccessToken("valid-token")).thenReturn(true);
		when(accessTokenProvider.getIdFromAccessToken("valid-token")).thenReturn(1L);

		// 모든 테스트에서 기본적으로 인증된 사용자가 있도록 설정
		AuthenticationContextHolder.setCurrentMemberId(1L);
	}

	@AfterEach
	void tearDown() {
		AuthenticationContextHolder.clear();
	}

	@Test
	@DisplayName("[Success] 커리큘럼 삭제 요청이 성공하면 204 No Content를 반환한다")
	void deleteCurriculum_Success() throws Exception {
		doNothing().when(curriculumDeletion).deleteCurriculum(anyLong(), anyLong());

		mockMvc.perform(delete("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isNoContent());

		verify(curriculumDeletion).deleteCurriculum(1L, 10L);
	}

	@Test
	@DisplayName("[Failure] 인증 정보가 없으면 401 Unauthorized를 반환한다")
	void deleteCurriculum_Fail_Unauthenticated() throws Exception {
		doThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND))
			.when(curriculumDeletion).deleteCurriculum(anyLong(), anyLong());

		mockMvc.perform(delete("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("[Failure] 권한이 없으면 403 Forbidden을 반환한다")
	void deleteCurriculum_Fail_Authorization() throws Exception {
		doThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED))
			.when(curriculumDeletion).deleteCurriculum(anyLong(), anyLong());

		mockMvc.perform(delete("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHORIZATION_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("[Failure] 커리큘럼이 존재하지 않으면 400 Bad Request를 반환한다")
	void deleteCurriculum_Fail_CurriculumNotFound() throws Exception {
		doThrow(new IllegalArgumentException("해당 과정에 존재하지 않는 커리큘럼입니다. ID: 999"))
			.when(curriculumDeletion).deleteCurriculum(anyLong(), anyLong());

		mockMvc.perform(delete("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 999L)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Success] 유효한 courseId와 curriculumId로 삭제 요청이 성공한다")
	void deleteCurriculum_ValidIds_Success() throws Exception {
		doNothing().when(curriculumDeletion).deleteCurriculum(5L, 25L);

		mockMvc.perform(delete("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 5L, 25L)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isNoContent());

		verify(curriculumDeletion).deleteCurriculum(5L, 25L);
	}

	@Test
	@DisplayName("[Behavior] DELETE 메서드로 정확한 엔드포인트에 요청이 전달되는지 확인한다")
	void deleteCurriculum_VerifyEndpoint() throws Exception {
		doNothing().when(curriculumDeletion).deleteCurriculum(anyLong(), anyLong());

		mockMvc.perform(delete("/api/v1/courses/123/curriculums/456")
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isNoContent());

		verify(curriculumDeletion).deleteCurriculum(123L, 456L);
	}
}
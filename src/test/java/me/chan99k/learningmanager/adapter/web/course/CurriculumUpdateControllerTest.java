package me.chan99k.learningmanager.adapter.web.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.adapter.auth.AccessTokenProvider;
import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.course.provides.CurriculumInfoUpdate;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;

@WebMvcTest(controllers = CurriculumUpdateController.class)
@Import(GlobalExceptionHandler.class)
class CurriculumUpdateControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CurriculumInfoUpdate curriculumInfoUpdate;

	@MockBean
	private AccessTokenProvider<Long> accessTokenProvider;

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
	@DisplayName("[Success] 커리큘럼 정보 수정 요청이 성공하면 200 OK를 반환한다")
	void updateCurriculum_Success() throws Exception {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request("Updated Title", "Updated Description");
		doNothing().when(curriculumInfoUpdate)
			.updateCurriculumInfo(anyLong(), anyLong(), any(CurriculumInfoUpdate.Request.class));

		// when
		MvcResult mvcResult = mockMvc.perform(put("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk());

		verify(curriculumInfoUpdate).updateCurriculumInfo(1L, 10L, request);
	}

	@Test
	@DisplayName("[Success] 제목만 수정 요청이 성공하면 200 OK를 반환한다")
	void updateCurriculum_TitleOnly_Success() throws Exception {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request("Updated Title", null);
		doNothing().when(curriculumInfoUpdate)
			.updateCurriculumInfo(anyLong(), anyLong(), any(CurriculumInfoUpdate.Request.class));

		// when
		MvcResult mvcResult = mockMvc.perform(put("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[Failure] 제목이 null이면 400 Bad Request를 반환한다")
	void updateCurriculum_Fail_NullTitle() throws Exception {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request(null, "Description");

		// when & then
		mockMvc.perform(put("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Validation Error"))
			.andExpect(jsonPath("$.detail").value("제목을 입력해주세요"));
	}

	@Test
	@DisplayName("[Failure] 요청 본문이 비어있으면 400 Bad Request를 반환한다")
	void updateCurriculum_Fail_EmptyBody() throws Exception {
		// when & then
		mockMvc.perform(put("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 인증 정보가 없으면 401 Unauthorized를 반환한다")
	void updateCurriculum_Fail_Unauthenticated() throws Exception {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request("Updated Title", "Updated Description");
		doThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND))
			.when(curriculumInfoUpdate)
			.updateCurriculumInfo(anyLong(), anyLong(), any(CurriculumInfoUpdate.Request.class));

		// when
		MvcResult mvcResult = mockMvc.perform(put("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("[Failure] 권한이 없으면 403 Forbidden을 반환한다")
	void updateCurriculum_Fail_Authorization() throws Exception {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request("Updated Title", "Updated Description");
		doThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED))
			.when(curriculumInfoUpdate)
			.updateCurriculumInfo(anyLong(), anyLong(), any(CurriculumInfoUpdate.Request.class));

		// when
		MvcResult mvcResult = mockMvc.perform(put("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 10L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHORIZATION_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("[Failure] 커리큘럼이 존재하지 않으면 400 Bad Request를 반환한다")
	void updateCurriculum_Fail_CurriculumNotFound() throws Exception {
		// given
		CurriculumInfoUpdate.Request request = new CurriculumInfoUpdate.Request("Updated Title", "Updated Description");
		doThrow(new IllegalArgumentException("커리큘럼을 찾을 수 없습니다"))
			.when(curriculumInfoUpdate)
			.updateCurriculumInfo(anyLong(), anyLong(), any(CurriculumInfoUpdate.Request.class));

		// when
		MvcResult mvcResult = mockMvc.perform(put("/api/v1/courses/{courseId}/curriculums/{curriculumId}", 1L, 999L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isBadRequest());
	}
}
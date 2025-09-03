package me.chan99k.learningmanager.adapter.web.session;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.List;

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
import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.adapter.auth.BcryptPasswordEncoder;
import me.chan99k.learningmanager.adapter.auth.JwtCredentialProvider;
import me.chan99k.learningmanager.adapter.auth.jwt.AccessJwtTokenProvider;
import me.chan99k.learningmanager.adapter.auth.jwt.InMemoryJwtTokenRevocationProvider;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.session.SessionParticipantService;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.AddParticipantRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.ChangeParticipantRoleRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.ChangeRoleDto;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.ParticipantInfo;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.RemoveParticipantRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.SessionParticipantResponse;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.session.SessionParticipantRole;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@WebMvcTest(SessionParticipantController.class)
@Import({
	GlobalExceptionHandler.class,
	JwtCredentialProvider.class,
	AccessJwtTokenProvider.class,
	InMemoryJwtTokenRevocationProvider.class,
	BcryptPasswordEncoder.class
})
class SessionParticipantControllerTest {

	private final Long sessionId = 1L;
	private final Long memberId = 100L;
	@MockBean
	AccessTokenProvider<Long> accessTokenProvider;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private SessionParticipantService sessionParticipantService;
	@MockBean
	private AsyncTaskExecutor sessionTaskExecutor;

	@BeforeEach
	void setUp() {
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run(); // 즉시 실행
			return null;
		}).given(sessionTaskExecutor).execute(any(Runnable.class));

		AuthenticationContextHolder.setCurrentMemberId(1L);

		given(accessTokenProvider.validateAccessToken("valid-token")).willReturn(true);
		given(accessTokenProvider.getIdFromAccessToken("valid-token")).willReturn(1L);
	}

	@AfterEach
	void tearDown() {
		AuthenticationContextHolder.clear();
	}

	@Test
	@DisplayName("참여자 추가 API - 성공")
	void addParticipant_Success() throws Exception {
		// given
		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);
		var response = new SessionParticipantResponse(
			sessionId,
			"테스트 세션",
			List.of(new ParticipantInfo(memberId, SessionParticipantRole.ATTENDEE))
		);

		when(sessionParticipantService.addParticipant(eq(sessionId), any(AddParticipantRequest.class)))
			.thenReturn(response);

		MvcResult mvcResult = mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.sessionId").value(sessionId))
			.andExpect(jsonPath("$.title").value("테스트 세션"))
			.andExpect(jsonPath("$.participants[0].memberId").value(memberId))
			.andExpect(jsonPath("$.participants[0].role").value("ATTENDEE"));

		verify(sessionParticipantService).addParticipant(eq(sessionId), any(AddParticipantRequest.class));
	}

	@Test
	@DisplayName("참여자 추가 API - 권한 없음")
	void addParticipant_NoPermission() throws Exception {
		// given
		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		when(sessionParticipantService.addParticipant(eq(sessionId), any(AddParticipantRequest.class)))
			.thenThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		// when & then
		MvcResult mvcResult = mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("참여자 추가 API - 세션 없음")
	void addParticipant_SessionNotFound() throws Exception {
		// given
		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		when(sessionParticipantService.addParticipant(eq(sessionId), any(AddParticipantRequest.class)))
			.thenThrow(new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		MvcResult mvcResult = mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("참여자 추가 API - 잘못된 요청 데이터")
	void addParticipant_InvalidRequest() throws Exception {
		String invalidRequest = "{\"memberId\": null, \"role\": \"ATTENDEE\"}";

		mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("참여자 제거 API - 성공")
	void removeParticipant_Success() throws Exception {
		// given
		var response = new SessionParticipantResponse(
			sessionId,
			"테스트 세션",
			List.of() // 참여자 제거 후 빈 목록
		);

		when(sessionParticipantService.removeParticipant(any(RemoveParticipantRequest.class)))
			.thenReturn(response);

		// when & then
		MvcResult mvcResult = mockMvc.perform(
				delete("/api/v1/sessions/{sessionId}/participants/{memberId}", sessionId, memberId))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionId").value(sessionId))
			.andExpect(jsonPath("$.title").value("테스트 세션"))
			.andExpect(jsonPath("$.participants").isEmpty());

		verify(sessionParticipantService).removeParticipant(any(RemoveParticipantRequest.class));
	}

	@Test
	@DisplayName("참여자 제거 API - 권한 없음")
	void removeParticipant_NoPermission() throws Exception {
		when(sessionParticipantService.removeParticipant(any(RemoveParticipantRequest.class)))
			.thenThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		MvcResult mvcResult = mockMvc.perform(
				delete("/api/v1/sessions/{sessionId}/participants/{memberId}", sessionId, memberId))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("참여자 역할 변경 API - 성공")
	void changeParticipantRole_Success() throws Exception {
		// given
		var request = new ChangeRoleDto(SessionParticipantRole.SPEAKER);
		var response = new SessionParticipantResponse(
			sessionId,
			"테스트 세션",
			List.of(new ParticipantInfo(memberId, SessionParticipantRole.SPEAKER))
		);

		when(sessionParticipantService.changeParticipantRole(any(ChangeParticipantRoleRequest.class)))
			.thenReturn(response);

		// when & then
		MvcResult mvcResult = mockMvc.perform(
				put("/api/v1/sessions/{sessionId}/participants/{memberId}/role", sessionId, memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionId").value(sessionId))
			.andExpect(jsonPath("$.title").value("테스트 세션"))
			.andExpect(jsonPath("$.participants[0].memberId").value(memberId))
			.andExpect(jsonPath("$.participants[0].role").value("SPEAKER"));

		verify(sessionParticipantService).changeParticipantRole(any(ChangeParticipantRoleRequest.class));
	}

	@Test
	@DisplayName("참여자 역할 변경 API - 권한 없음")
	void changeParticipantRole_NoPermission() throws Exception {
		// given
		var request = new ChangeRoleDto(SessionParticipantRole.HOST);

		when(sessionParticipantService.changeParticipantRole(any(ChangeParticipantRoleRequest.class)))
			.thenThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		MvcResult mvcResult = mockMvc.perform(
				put("/api/v1/sessions/{sessionId}/participants/{memberId}/role", sessionId, memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("참여자 역할 변경 API - 잘못된 요청 데이터")
	void changeParticipantRole_InvalidRequest() throws Exception {
		// given - newRole이 null인 잘못된 요청
		String invalidRequest = "{\"newRole\": null}";

		mockMvc.perform(put("/api/v1/sessions/{sessionId}/participants/{memberId}/role", sessionId, memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("비동기 처리 확인")
	void asyncProcessingVerification() throws Exception {
		// given
		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);
		var response = new SessionParticipantResponse(sessionId, "테스트 세션", List.of());

		when(sessionParticipantService.addParticipant(eq(sessionId), any(AddParticipantRequest.class)))
			.thenReturn(response);

		mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted()) // 비동기 요청이 시작되었는지 확인
			.andDo(result -> {
				// 비동기 처리 완료 후 결과 확인
				mockMvc.perform(asyncDispatch(result))
					.andExpect(status().isCreated());
			});

		verify(sessionTaskExecutor).execute(any(Runnable.class));
	}
}
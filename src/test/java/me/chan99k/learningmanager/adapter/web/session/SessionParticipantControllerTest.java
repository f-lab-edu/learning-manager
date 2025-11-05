package me.chan99k.learningmanager.adapter.web.session;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.session.SessionParticipantService;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.AddParticipantRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.ChangeParticipantRoleRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.ChangeRoleDto;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.LeaveSessionRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.ParticipantInfo;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.RemoveParticipantRequest;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement.SessionParticipantResponse;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.session.SessionParticipantRole;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@WebMvcTest(controllers = SessionParticipantController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import({
	GlobalExceptionHandler.class
})
class SessionParticipantControllerTest {

	private final Long sessionId = 1L;
	private final Long memberId = 100L;
	@MockBean
	UserContext userContext;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private SessionParticipantService sessionParticipantService;

	@BeforeEach
	void setUp() {
		given(userContext.getCurrentMemberId()).willReturn(1L);
		given(userContext.isAuthenticated()).willReturn(true);
	}


	@Test
	@DisplayName("참여자 추가 API - 성공")
	void addParticipant_Success() throws Exception {
		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);
		var response = new SessionParticipantResponse(
			sessionId,
			"테스트 세션",
			List.of(new ParticipantInfo(memberId, SessionParticipantRole.ATTENDEE))
		);

		when(sessionParticipantService.addParticipant(eq(sessionId), any(AddParticipantRequest.class)))
			.thenReturn(response);

		mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		verify(sessionParticipantService).addParticipant(eq(sessionId), any(AddParticipantRequest.class));
	}

	@Test
	@DisplayName("참여자 추가 API - 권한 없음")
	void addParticipant_NoPermission() throws Exception {
		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		when(sessionParticipantService.addParticipant(eq(sessionId), any(AddParticipantRequest.class)))
			.thenThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("참여자 추가 API - 세션 없음")
	void addParticipant_SessionNotFound() throws Exception {
		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		when(sessionParticipantService.addParticipant(eq(sessionId), any(AddParticipantRequest.class)))
			.thenThrow(new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", sessionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
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
		var response = new SessionParticipantResponse(
			sessionId,
			"테스트 세션",
			List.of() // 참여자 제거 후 빈 목록
		);

		when(sessionParticipantService.removeParticipant(any(RemoveParticipantRequest.class)))
			.thenReturn(response);

		mockMvc.perform(
				delete("/api/v1/sessions/{sessionId}/participants/{memberId}", sessionId, memberId))
			.andExpect(status().isNoContent());

		verify(sessionParticipantService).removeParticipant(any(RemoveParticipantRequest.class));
	}

	@Test
	@DisplayName("참여자 제거 API - 권한 없음")
	void removeParticipant_NoPermission() throws Exception {
		when(sessionParticipantService.removeParticipant(any(RemoveParticipantRequest.class)))
			.thenThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		mockMvc.perform(
				delete("/api/v1/sessions/{sessionId}/participants/{memberId}", sessionId, memberId))
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
		mockMvc.perform(
				put("/api/v1/sessions/{sessionId}/participants/{memberId}/role", sessionId, memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		verify(sessionParticipantService).changeParticipantRole(any(ChangeParticipantRoleRequest.class));
	}

	@Test
	@DisplayName("참여자 역할 변경 API - 권한 없음")
	void changeParticipantRole_NoPermission() throws Exception {
		var request = new ChangeRoleDto(SessionParticipantRole.HOST);

		when(sessionParticipantService.changeParticipantRole(any(ChangeParticipantRoleRequest.class)))
			.thenThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));

		mockMvc.perform(
				put("/api/v1/sessions/{sessionId}/participants/{memberId}/role", sessionId, memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
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
	@DisplayName("자가 탈퇴 API - 성공")
	void leaveSession_Success() throws Exception {
		var response = new SessionParticipantResponse(
			sessionId,
			"테스트 하위 세션",
			List.of() // 자가 탈퇴 후 빈 목록
		);

		when(sessionParticipantService.leaveSession(any(LeaveSessionRequest.class)))
			.thenReturn(response);

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/me", sessionId))
			.andExpect(status().isNoContent());

		verify(sessionParticipantService).leaveSession(any(LeaveSessionRequest.class));
	}

	@Test
	@DisplayName("자가 탈퇴 API - 루트 세션에서 실패")
	void leaveSession_RootSession_Fail() throws Exception {
		when(sessionParticipantService.leaveSession(any(LeaveSessionRequest.class)))
			.thenThrow(new DomainException(SessionProblemCode.ROOT_SESSION_SELF_LEAVE_NOT_ALLOWED));

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/me", sessionId))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("자가 탈퇴 API - HOST가 혼자인 경우 실패")
	void leaveSession_HostAlone_Fail() throws Exception {
		when(sessionParticipantService.leaveSession(any(LeaveSessionRequest.class)))
			.thenThrow(new DomainException(SessionProblemCode.HOST_CANNOT_LEAVE_ALONE));

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/me", sessionId))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("자가 탈퇴 API - 세션 없음")
	void leaveSession_SessionNotFound() throws Exception {
		when(sessionParticipantService.leaveSession(any(LeaveSessionRequest.class)))
			.thenThrow(new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/me", sessionId))
			.andExpect(status().isBadRequest());
	}

}
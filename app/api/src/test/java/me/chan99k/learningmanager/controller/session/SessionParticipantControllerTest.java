package me.chan99k.learningmanager.controller.session;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.SessionSecurity;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.config.SecurityConfig;
import me.chan99k.learningmanager.filter.JwtAuthenticationFilter;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.session.SessionParticipantManagement;
import me.chan99k.learningmanager.session.SessionParticipantRole;

/**
 * SessionParticipantController 테스트
 *
 * @PreAuthorize("@sessionSecurity.canManageSessionParticipants(...)") 검증을 포함합니다.
 */
@WebMvcTest(controllers = SessionParticipantController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("SessionParticipantController 테스트")
class SessionParticipantControllerTest {

	private static final Long MANAGER_ID = 1L;
	private static final Long NON_MANAGER_ID = 2L;
	private static final Long TARGET_MEMBER_ID = 100L;
	private static final Long SESSION_ID = 10L;
	private static final String MANAGER_EMAIL = "manager@example.com";
	private static final String VALID_TOKEN = "valid-jwt-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SessionParticipantManagement sessionParticipantManagement;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private SystemAuthorizationPort systemAuthorizationPort;

	@MockBean(name = "sessionSecurity")
	private SessionSecurity sessionSecurity;

	@BeforeEach
	void setUp() {
		// 기본 인증 설정: MANAGER_ID로 인증된 사용자
		JwtProvider.Claims claims = new JwtProvider.Claims(
			MANAGER_ID,
			MANAGER_EMAIL,
			Instant.now().plusSeconds(3600)
		);

		given(jwtProvider.isValid(VALID_TOKEN)).willReturn(true);
		given(jwtProvider.validateAndGetClaims(VALID_TOKEN)).willReturn(claims);
		given(systemAuthorizationPort.getRoles(MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));

		// 기본: MANAGER_ID는 세션 참여자 관리 권한 있음
		given(sessionSecurity.canManageSessionParticipants(SESSION_ID, MANAGER_ID)).willReturn(true);
		given(sessionSecurity.isSessionMember(SESSION_ID, MANAGER_ID)).willReturn(true);
	}

	@Nested
	@DisplayName("참여자 추가 API (POST /api/v1/sessions/{sessionId}/participants)")
	class AddParticipantTest {

		static Stream<Arguments> invalidAddParticipantRequestProvider() {
			return Stream.of(
				Arguments.of("memberId 누락", "{\"role\":\"ATTENDEE\"}"),
				Arguments.of("역할 누락", "{\"memberId\":100}")
			);
		}

		@Test
		@DisplayName("[Success] 참여자 추가 성공 - 201 Created")
		void add_participant_success() throws Exception {
			SessionParticipantManagement.AddParticipantRequest request =
				new SessionParticipantManagement.AddParticipantRequest(TARGET_MEMBER_ID,
					SessionParticipantRole.ATTENDEE);

			SessionParticipantManagement.SessionParticipantResponse response =
				new SessionParticipantManagement.SessionParticipantResponse(
					SESSION_ID,
					"테스트 세션",
					List.of(new SessionParticipantManagement.ParticipantInfo(TARGET_MEMBER_ID,
						SessionParticipantRole.ATTENDEE))
				);

			given(sessionParticipantManagement.addParticipant(eq(MANAGER_ID), eq(SESSION_ID), any()))
				.willReturn(response);

			mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", SESSION_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated());

			then(sessionParticipantManagement).should().addParticipant(eq(MANAGER_ID), eq(SESSION_ID), any());
		}

		@ParameterizedTest(name = "[Failure] {0} - 400 Bad Request")
		@MethodSource("invalidAddParticipantRequestProvider")
		@DisplayName("[Failure] 유효하지 않은 요청 - 400 Bad Request")
		void add_participant_with_invalid_request(String testCase, String requestJson) throws Exception {
			mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", SESSION_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

			then(sessionParticipantManagement).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void add_participant_without_token() throws Exception {
			SessionParticipantManagement.AddParticipantRequest request =
				new SessionParticipantManagement.AddParticipantRequest(TARGET_MEMBER_ID,
					SessionParticipantRole.ATTENDEE);

			mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", SESSION_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(sessionParticipantManagement).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 참여자 관리 권한 없음 - 403 Forbidden")
		void add_participant_without_permission() throws Exception {
			String nonManagerToken = "non-manager-token";
			JwtProvider.Claims nonManagerClaims = new JwtProvider.Claims(
				NON_MANAGER_ID,
				"user@example.com",
				Instant.now().plusSeconds(3600)
			);

			given(jwtProvider.isValid(nonManagerToken)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(nonManagerToken)).willReturn(nonManagerClaims);
			given(systemAuthorizationPort.getRoles(NON_MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));
			given(sessionSecurity.canManageSessionParticipants(SESSION_ID, NON_MANAGER_ID)).willReturn(false);

			SessionParticipantManagement.AddParticipantRequest request =
				new SessionParticipantManagement.AddParticipantRequest(TARGET_MEMBER_ID,
					SessionParticipantRole.ATTENDEE);

			mockMvc.perform(post("/api/v1/sessions/{sessionId}/participants", SESSION_ID)
					.header("Authorization", "Bearer " + nonManagerToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(sessionParticipantManagement).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("참여자 제거 API (DELETE /api/v1/sessions/{sessionId}/participants/{memberId})")
	class RemoveParticipantTest {

		@Test
		@DisplayName("[Success] 참여자 제거 성공 - 204 No Content")
		void remove_participant_success() throws Exception {
			SessionParticipantManagement.SessionParticipantResponse response =
				new SessionParticipantManagement.SessionParticipantResponse(SESSION_ID, "테스트 세션", List.of());

			given(sessionParticipantManagement.removeParticipant(eq(MANAGER_ID), any()))
				.willReturn(response);

			mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/{memberId}",
					SESSION_ID, TARGET_MEMBER_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN))
				.andDo(print())
				.andExpect(status().isNoContent());

			then(sessionParticipantManagement).should().removeParticipant(eq(MANAGER_ID), any());
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void remove_participant_without_token() throws Exception {
			mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/{memberId}",
					SESSION_ID, TARGET_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(sessionParticipantManagement).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("참여자 역할 변경 API (PUT /api/v1/sessions/{sessionId}/participants/{memberId}/role)")
	class ChangeParticipantRoleTest {

		@Test
		@DisplayName("[Success] 역할 변경 성공 - 204 No Content")
		void change_role_success() throws Exception {
			SessionParticipantManagement.ChangeRoleDto request =
				new SessionParticipantManagement.ChangeRoleDto(SessionParticipantRole.HOST);

			SessionParticipantManagement.SessionParticipantResponse response =
				new SessionParticipantManagement.SessionParticipantResponse(
					SESSION_ID,
					"테스트 세션",
					List.of(
						new SessionParticipantManagement.ParticipantInfo(TARGET_MEMBER_ID, SessionParticipantRole.HOST))
				);

			given(sessionParticipantManagement.changeParticipantRole(eq(MANAGER_ID), any()))
				.willReturn(response);

			mockMvc.perform(put("/api/v1/sessions/{sessionId}/participants/{memberId}/role",
					SESSION_ID, TARGET_MEMBER_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isNoContent());

			then(sessionParticipantManagement).should().changeParticipantRole(eq(MANAGER_ID), any());
		}

		@Test
		@DisplayName("[Failure] 새 역할 누락 - 400 Bad Request")
		void change_role_without_new_role() throws Exception {
			String requestJson = "{}";

			mockMvc.perform(put("/api/v1/sessions/{sessionId}/participants/{memberId}/role",
					SESSION_ID, TARGET_MEMBER_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

			then(sessionParticipantManagement).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("세션 탈퇴 API (DELETE /api/v1/sessions/{sessionId}/participants/me)")
	class LeaveSessionTest {

		@Test
		@DisplayName("[Success] 세션 탈퇴 성공 - 204 No Content")
		void leave_session_success() throws Exception {
			SessionParticipantManagement.SessionParticipantResponse response =
				new SessionParticipantManagement.SessionParticipantResponse(SESSION_ID, "테스트 세션", List.of());

			given(sessionParticipantManagement.leaveSession(eq(MANAGER_ID), any()))
				.willReturn(response);

			mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/me", SESSION_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN))
				.andDo(print())
				.andExpect(status().isNoContent());

			then(sessionParticipantManagement).should().leaveSession(eq(MANAGER_ID), any());
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void leave_session_without_token() throws Exception {
			mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/me", SESSION_ID))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(sessionParticipantManagement).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 세션 멤버 아님 - 403 Forbidden")
		void leave_session_not_a_member() throws Exception {
			String nonMemberToken = "non-member-token";
			JwtProvider.Claims nonMemberClaims = new JwtProvider.Claims(
				NON_MANAGER_ID,
				"user@example.com",
				Instant.now().plusSeconds(3600)
			);

			given(jwtProvider.isValid(nonMemberToken)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(nonMemberToken)).willReturn(nonMemberClaims);
			given(systemAuthorizationPort.getRoles(NON_MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));
			given(sessionSecurity.isSessionMember(SESSION_ID, NON_MANAGER_ID)).willReturn(false);

			mockMvc.perform(delete("/api/v1/sessions/{sessionId}/participants/me", SESSION_ID)
					.header("Authorization", "Bearer " + nonMemberToken))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(sessionParticipantManagement).shouldHaveNoInteractions();
		}
	}
}

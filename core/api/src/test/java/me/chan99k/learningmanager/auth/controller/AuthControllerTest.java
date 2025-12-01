package me.chan99k.learningmanager.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.application.member.provides.MemberLogin;
import me.chan99k.learningmanager.auth.AuthService;
import me.chan99k.learningmanager.web.auth.AuthController;
import me.chan99k.learningmanager.web.auth.dto.LoginRequest;
import me.chan99k.learningmanager.web.auth.dto.LoginResponse;
import me.chan99k.learningmanager.web.auth.dto.RefreshRequest;

@WebMvcTest(controllers = AuthController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@MockBean
	private MemberLogin memberLoginService;

	@Test
	@DisplayName("유효한 리프레시 토큰으로 토큰 갱신 요청 시 새로운 토큰 쌍을 반환한다")
	void refresh_ValidRefreshToken_ReturnsNewTokenPair() throws Exception {
		String refreshToken = "valid-refresh-token";
		RefreshRequest request = new RefreshRequest(refreshToken);
		AuthService.TokenPair tokenPair = new AuthService.TokenPair("new-access-token", "new-refresh-token");

		when(authService.refreshTokens(refreshToken)).thenReturn(tokenPair);

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("new-access-token"))
			.andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
			.andExpect(jsonPath("$.tokenType").value("Bearer"));

		verify(authService).refreshTokens(refreshToken);
	}

	@Test
	@DisplayName("유효하지 않은 리프레시 토큰으로 요청 시 401 Unauthorized를 반환한다")
	void refresh_InvalidRefreshToken_ReturnsUnauthorized() throws Exception {
		String invalidRefreshToken = "invalid-refresh-token";
		RefreshRequest request = new RefreshRequest(invalidRefreshToken);

		when(authService.refreshTokens(invalidRefreshToken))
			.thenThrow(new me.chan99k.learningmanager.domain.exception.AuthenticationException(
				me.chan99k.learningmanager.domain.exception.AuthProblemCode.INVALID_TOKEN));

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());

		verify(authService).refreshTokens(invalidRefreshToken);
	}

	@Test
	@DisplayName("빈 리프레시 토큰으로 요청 시 400 Bad Request를 반환한다")
	void refresh_EmptyRefreshToken_ReturnsBadRequest() throws Exception {
		RefreshRequest request = new RefreshRequest("");

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("null 리프레시 토큰으로 요청 시 400 Bad Request를 반환한다")
	void refresh_NullRefreshToken_ReturnsBadRequest() throws Exception {
		RefreshRequest request = new RefreshRequest(null);

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("유효한 로그인 정보로 로그인 요청 시 토큰과 회원 정보를 반환한다")
	void login_ValidCredentials_ReturnsTokenAndMemberInfo() throws Exception {
		String email = "test@example.com";
		String password = "password123";
		LoginRequest request = new LoginRequest(email, password);
		LoginResponse response = LoginResponse.of("access-token", "refresh-token", 1L, email);

		when(authService.login(request)).thenReturn(response);

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("access-token"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token"))
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.memberId").value(1))
			.andExpect(jsonPath("$.email").value(email));

		verify(authService).login(request);
	}

	@Test
	@DisplayName("잘못된 이메일 형식으로 로그인 요청 시 400 Bad Request를 반환한다")
	void login_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
		LoginRequest request = new LoginRequest("invalid-email", "password123");

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("빈 비밀번호로 로그인 요청 시 400 Bad Request를 반환한다")
	void login_EmptyPassword_ReturnsBadRequest() throws Exception {
		LoginRequest request = new LoginRequest("test@example.com", "");

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("로그아웃 요청 시 리프레시 토큰을 무효화하고 성공 응답을 반환한다")
	void logout_ValidRefreshToken_RevokesTokenAndReturnsSuccess() throws Exception {
		String refreshToken = "valid-refresh-token";
		RefreshRequest request = new RefreshRequest(refreshToken);

		mockMvc.perform(post("/api/v1/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		verify(authService).revokeRefreshToken(refreshToken);
	}
}
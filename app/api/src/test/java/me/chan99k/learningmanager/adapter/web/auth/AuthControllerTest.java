package me.chan99k.learningmanager.adapter.web.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.authentication.AuthProblemCode;
import me.chan99k.learningmanager.authentication.IssueToken;
import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authentication.RefreshAccessToken;
import me.chan99k.learningmanager.authentication.RevokeAllTokens;
import me.chan99k.learningmanager.authentication.RevokeToken;
import me.chan99k.learningmanager.controller.auth.AuthController;
import me.chan99k.learningmanager.exception.DomainException;

@WebMvcTest(value = AuthController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@DisplayName("AuthController 테스트")
public class AuthControllerTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PASSWORD = "Password123!";
	private static final String TEST_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-access-token";
	private static final String TEST_REFRESH_TOKEN = "test-refresh-token-uuid";
	private static final long EXPIRES_IN_SECONDS = 3600L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private IssueToken issueToken;

	@MockBean
	private RefreshAccessToken refreshAccessToken;

	@MockBean
	private RevokeToken revokeToken;

	@MockBean
	private RevokeAllTokens revokeAllTokens;

	@MockBean
	private JwtProvider jwtProvider;

	@Nested
	@DisplayName("토큰 발급 API 테스트 (POST /api/v1/auth/token)")
	class IssueTokenTest {

		@Test
		@DisplayName("[Success] 유효한 자격증명으로 토큰 발급 성공 - 200 OK")
		void issue_token_test_01() throws Exception {
			// Given
			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);
			IssueToken.Response response = IssueToken.Response.of(
				TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, EXPIRES_IN_SECONDS
			);

			given(issueToken.issueToken(any(IssueToken.Request.class)))
				.willReturn(response);

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN))
				.andExpect(jsonPath("$.refreshToken").value(TEST_REFRESH_TOKEN))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(EXPIRES_IN_SECONDS));
		}

		@Test
		@DisplayName("[Failure] 이메일 누락 - 400 Bad Request")
		void issue_token_test_02() throws Exception {
			// Given
			String requestJson = "{\"password\":\"" + TEST_PASSWORD + "\"}";

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Failure] 비밀번호 누락 - 400 Bad Request")
		void issue_token_test_03() throws Exception {
			// Given
			String requestJson = "{\"email\":\"" + TEST_EMAIL + "\"}";

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Failure] 잘못된 자격증명 - 401 Unauthorized")
		void issue_token_test_04() throws Exception {
			// Given
			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, "wrongPassword");

			given(issueToken.issueToken(any(IssueToken.Request.class)))
				.willThrow(new DomainException(AuthProblemCode.INVALID_CREDENTIALS));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("AUTH001"));
		}

		@Test
		@DisplayName("[Failure] 예기치 못한 서버 오류 - 500 Internal Server Error")
		void issue_token_test_05() throws Exception {
			// Given
			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);

			given(issueToken.issueToken(any(IssueToken.Request.class)))
				.willThrow(new RuntimeException("데이터베이스 연결 오류"));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Internal Server Error"))
				.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
		}
	}

	@Nested
	@DisplayName("토큰 갱신 API 테스트 (POST /api/v1/auth/token/refresh)")
	class RefreshTokenTest {

		@Test
		@DisplayName("[Success] 유효한 리프레시 토큰으로 갱신 성공 - 200 OK")
		void refresh_token_test_01() throws Exception {
			// Given
			RefreshAccessToken.Request request = new RefreshAccessToken.Request(TEST_REFRESH_TOKEN);
			String newAccessToken = "new-access-token";
			String newRefreshToken = "new-refresh-token";
			RefreshAccessToken.Response response = RefreshAccessToken.Response.of(
				newAccessToken, newRefreshToken, EXPIRES_IN_SECONDS
			);

			given(refreshAccessToken.refresh(any(RefreshAccessToken.Request.class)))
				.willReturn(response);

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.accessToken").value(newAccessToken))
				.andExpect(jsonPath("$.refreshToken").value(newRefreshToken))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(EXPIRES_IN_SECONDS));
		}

		@Test
		@DisplayName("[Failure] 리프레시 토큰 누락 - 400 Bad Request")
		void refresh_token_test_02() throws Exception {
			// Given
			String requestJson = "{}";

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Failure] 만료된 토큰 - 401 Unauthorized")
		void refresh_token_test_03() throws Exception {
			// Given
			RefreshAccessToken.Request request = new RefreshAccessToken.Request("expired-token");

			given(refreshAccessToken.refresh(any(RefreshAccessToken.Request.class)))
				.willThrow(new DomainException(AuthProblemCode.EXPIRED_TOKEN));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("AUTH003"));
		}

		@Test
		@DisplayName("[Failure] 폐기된 토큰 - 401 Unauthorized")
		void refresh_token_test_04() throws Exception {
			// Given
			RefreshAccessToken.Request request = new RefreshAccessToken.Request("revoked-token");

			given(refreshAccessToken.refresh(any(RefreshAccessToken.Request.class)))
				.willThrow(new DomainException(AuthProblemCode.REVOKED_TOKEN));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("AUTH004"));
		}

		@Test
		@DisplayName("[Failure] 존재하지 않는 토큰 - 404 Not Found")
		void refresh_token_test_05() throws Exception {
			// Given
			RefreshAccessToken.Request request = new RefreshAccessToken.Request("not-found-token");

			given(refreshAccessToken.refresh(any(RefreshAccessToken.Request.class)))
				.willThrow(new DomainException(AuthProblemCode.TOKEN_NOT_FOUND));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("AUTH005"));
		}
	}

	@Nested
	@DisplayName("토큰 폐기 API 테스트 (POST /api/v1/auth/token/revoke)")
	class RevokeTokenTest {

		@Test
		@DisplayName("[Success] 토큰 폐기 성공 - 200 OK")
		void revoke_token_test_01() throws Exception {
			// Given
			RevokeToken.Request request = new RevokeToken.Request(TEST_REFRESH_TOKEN, "refresh_token");

			doNothing().when(revokeToken).revoke(any(RevokeToken.Request.class));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/revoke")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("[Success] tokenTypeHint 없이 토큰 폐기 성공 - 200 OK")
		void revoke_token_test_02() throws Exception {
			// Given
			RevokeToken.Request request = new RevokeToken.Request(TEST_REFRESH_TOKEN);

			doNothing().when(revokeToken).revoke(any(RevokeToken.Request.class));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/revoke")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("[Failure] 토큰 누락 - 400 Bad Request")
		void revoke_token_test_03() throws Exception {
			// Given
			String requestJson = "{}";

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/revoke")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Success] 존재하지 않는 토큰도 성공 응답 - 200 OK (RFC 7009 권장)")
		void revoke_token_test_04() throws Exception {
			// Given - RFC 7009에 따르면 존재하지 않는 토큰 폐기 요청도 200 OK 반환
			RevokeToken.Request request = new RevokeToken.Request("not-found-token");

			doNothing().when(revokeToken).revoke(any(RevokeToken.Request.class));

			// When & Then
			mockMvc.perform(post("/api/v1/auth/token/revoke")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk());
		}
	}
}

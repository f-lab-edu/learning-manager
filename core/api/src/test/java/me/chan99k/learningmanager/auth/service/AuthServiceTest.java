package me.chan99k.learningmanager.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthService;
import me.chan99k.learningmanager.adapter.auth.RefreshTokenAdapter;
import me.chan99k.learningmanager.adapter.web.auth.dto.LoginRequest;
import me.chan99k.learningmanager.adapter.web.auth.dto.LoginResponse;
import me.chan99k.learningmanager.application.member.provides.MemberLogin;
import me.chan99k.learningmanager.application.member.requires.RefreshTokenProvider.RefreshResult;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private RefreshTokenAdapter refreshTokenAdapter;

	@Mock
	private MemberLogin memberLoginService;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("유효한 로그인 정보로 로그인할 수 있다")
	void login_ValidCredentials_ReturnsLoginResponse() {
		String email = "test@example.com";
		String password = "password123";
		LoginRequest request = new LoginRequest(email, password);

		MemberLogin.Response memberLoginResponse = new MemberLogin.Response(
			"access-token", "refresh-token", 1L, email);

		when(memberLoginService.login(any(MemberLogin.Request.class))).thenReturn(memberLoginResponse);

		LoginResponse result = authService.login(request);

		assertThat(result.accessToken()).isEqualTo("access-token");
		assertThat(result.refreshToken()).isEqualTo("refresh-token");
		assertThat(result.tokenType()).isEqualTo("Bearer");
		assertThat(result.memberId()).isEqualTo(1L);
		assertThat(result.email()).isEqualTo(email);

		verify(memberLoginService).login(any(MemberLogin.Request.class));
	}

	@Test
	@DisplayName("유효한 리프레시 토큰으로 새로운 토큰 쌍을 발급받을 수 있다")
	void refreshTokens_ValidRefreshToken_ReturnsNewTokenPair() {
		String refreshToken = "valid-refresh-token";
		String newAccessToken = "new-access-token";
		String newRefreshToken = "new-refresh-token";
		RefreshResult refreshResult = new RefreshResult(newAccessToken, newRefreshToken);

		when(refreshTokenAdapter.refreshAccessToken(refreshToken)).thenReturn(refreshResult);

		AuthService.TokenPair result = authService.refreshTokens(refreshToken);

		assertThat(result.accessToken()).isEqualTo(newAccessToken);
		assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
		verify(refreshTokenAdapter).refreshAccessToken(refreshToken);
	}

	@Test
	@DisplayName("유효하지 않은 리프레시 토큰으로 요청 시 예외가 발생한다")
	void refreshTokens_InvalidRefreshToken_ThrowsException() {
		String invalidRefreshToken = "invalid-refresh-token";
		when(refreshTokenAdapter.refreshAccessToken(invalidRefreshToken))
			.thenThrow(new IllegalArgumentException("Invalid refresh token"));

		assertThatThrownBy(() -> authService.refreshTokens(invalidRefreshToken))
			.isInstanceOf(me.chan99k.learningmanager.domain.exception.AuthenticationException.class)
			.hasMessage("[System] 유효하지 않은 토큰입니다");

		verify(refreshTokenAdapter).refreshAccessToken(invalidRefreshToken);
	}

	@Test
	@DisplayName("리프레시 토큰을 무효화할 수 있다")
	void revokeRefreshToken_ValidRefreshToken_RevokesToken() {
		String refreshToken = "refresh-token-to-revoke";

		authService.revokeRefreshToken(refreshToken);

		verify(refreshTokenAdapter).revokeRefreshToken(refreshToken);
	}
}
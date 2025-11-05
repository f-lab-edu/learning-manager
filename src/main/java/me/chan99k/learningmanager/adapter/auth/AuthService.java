package me.chan99k.learningmanager.adapter.auth;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.adapter.web.auth.dto.LoginRequest;
import me.chan99k.learningmanager.adapter.web.auth.dto.LoginResponse;
import me.chan99k.learningmanager.application.member.provides.MemberLogin;
import me.chan99k.learningmanager.application.member.requires.RefreshTokenProvider.RefreshResult;

@Service
public class AuthService {
	private final RefreshTokenAdapter refreshTokenAdapter;
	private final MemberLogin memberLoginService;

	public AuthService(RefreshTokenAdapter refreshTokenAdapter, MemberLogin memberLoginService) {
		this.refreshTokenAdapter = refreshTokenAdapter;
		this.memberLoginService = memberLoginService;
	}

	public LoginResponse login(LoginRequest request) {
		MemberLogin.Request memberLoginRequest = new MemberLogin.Request(request.email(), request.password());
		MemberLogin.Response memberLoginResponse = memberLoginService.login(memberLoginRequest);

		return LoginResponse.of(
			memberLoginResponse.accessToken(),
			memberLoginResponse.refreshToken(),
			memberLoginResponse.memberId(),
			memberLoginResponse.email()
		);
	}

	public TokenPair refreshTokens(String refreshToken) {
		try {
			RefreshResult result = refreshTokenAdapter.refreshAccessToken(refreshToken);
			return new TokenPair(result.newAccessToken(), result.newRefreshToken());
		} catch (IllegalArgumentException e) {
			throw new me.chan99k.learningmanager.common.exception.AuthenticationException(
				AuthProblemCode.INVALID_TOKEN, e);
		}
	}

	public void revokeRefreshToken(String refreshToken) {
		refreshTokenAdapter.revokeRefreshToken(refreshToken);
	}

	public record TokenPair(String accessToken, String refreshToken) {
	}
}
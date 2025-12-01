package me.chan99k.learningmanager.web.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.auth.AuthService;
import me.chan99k.learningmanager.web.auth.dto.LoginRequest;
import me.chan99k.learningmanager.web.auth.dto.LoginResponse;
import me.chan99k.learningmanager.web.auth.dto.RefreshRequest;
import me.chan99k.learningmanager.web.auth.dto.TokenResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
		LoginResponse response = authService.login(request);

		return ResponseEntity.ok(response);
	}

	/**
	 * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급
	 *
	 * @param request 리프레시 토큰 요청
	 * @return 새로운 토큰 쌍
	 */
	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest request) {
		AuthService.TokenPair tokenPair = authService.refreshTokens(request.refreshToken());

		TokenResponse response = TokenResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());

		return ResponseEntity.ok(response);
	}

	/**
	 * 로그아웃 (명시적 리프레시 토큰 무효화)
	 *
	 * @param request 리프레시 토큰 요청
	 * @return 로그아웃 완료 응답
	 */
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequest request) {
		authService.revokeRefreshToken(request.refreshToken());

		return ResponseEntity.ok().build();
	}
}
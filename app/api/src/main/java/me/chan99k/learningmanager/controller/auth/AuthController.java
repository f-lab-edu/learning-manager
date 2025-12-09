package me.chan99k.learningmanager.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.chan99k.learningmanager.authentication.IssueToken;
import me.chan99k.learningmanager.authentication.RefreshAccessToken;
import me.chan99k.learningmanager.authentication.RevokeAllTokens;
import me.chan99k.learningmanager.authentication.RevokeToken;
import me.chan99k.learningmanager.security.CustomUserDetails;

@Tag(name = "Authentication", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final IssueToken issueToken;
	private final RefreshAccessToken refreshAccessToken;
	private final RevokeToken revokeToken;
	private final RevokeAllTokens revokeAllTokens;

	public AuthController(
		IssueToken issueToken,
		RefreshAccessToken refreshAccessToken,
		RevokeToken revokeToken,
		RevokeAllTokens revokeAllTokens
	) {
		this.issueToken = issueToken;
		this.refreshAccessToken = refreshAccessToken;
		this.revokeToken = revokeToken;
		this.revokeAllTokens = revokeAllTokens;
	}

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 Access Token과 Refresh Token을 발급받습니다.")
	@PostMapping("/token")
	public ResponseEntity<IssueToken.Response> issueToken(
		@Valid @RequestBody IssueToken.Request request
	) {
		IssueToken.Response response = issueToken.issueToken(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/token/refresh")
	public ResponseEntity<RefreshAccessToken.Response> refreshToken(
		@Valid @RequestBody RefreshAccessToken.Request request
	) {
		RefreshAccessToken.Response response = refreshAccessToken.refresh(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/token/revoke")
	public ResponseEntity<Void> revokeToken(
		@Valid @RequestBody RevokeToken.Request request
	) {
		revokeToken.revoke(request);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "전체 세션 로그아웃", description = "모든 기기에서 로그아웃합니다. 현재 사용자의 모든 Refresh Token을 폐기합니다.")
	@PostMapping("/token/revoke-all")
	public ResponseEntity<Void> revokeAllTokens(
		@AuthenticationPrincipal CustomUserDetails user
	) {
		revokeAllTokens.revokeAll(user.getMemberId());
		return ResponseEntity.ok().build();
	}
}

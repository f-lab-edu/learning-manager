package me.chan99k.learningmanager.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.auth.IssueToken;
import me.chan99k.learningmanager.auth.RefreshAccessToken;
import me.chan99k.learningmanager.auth.RevokeToken;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final IssueToken issueToken;
	private final RefreshAccessToken refreshAccessToken;
	private final RevokeToken revokeToken;

	public AuthController(
		IssueToken issueToken,
		RefreshAccessToken refreshAccessToken,
		RevokeToken revokeToken
	) {
		this.issueToken = issueToken;
		this.refreshAccessToken = refreshAccessToken;
		this.revokeToken = revokeToken;
	}

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
}

package me.chan99k.learningmanager.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.member.PasswordChange;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/members")
public class PasswordChangeController {
	private final PasswordChange passwordChange;

	public PasswordChangeController(PasswordChange passwordChange) {
		this.passwordChange = passwordChange;
	}

	@PutMapping("/password")
	public ResponseEntity<Void> changePassword(
		@AuthenticationPrincipal CustomUserDetails user,
		@Valid @RequestBody PasswordChange.Request request
	) {
		passwordChange.changePassword(user.getMemberId(), request);
		return ResponseEntity.ok().build();
	}
}

package me.chan99k.learningmanager.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.member.PasswordResetConfirmation;
import me.chan99k.learningmanager.member.PasswordResetRequest;
import me.chan99k.learningmanager.member.PasswordResetVerification;

@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordResetController {

	private final PasswordResetRequest passwordResetRequest;
	private final PasswordResetVerification passwordResetVerification;
	private final PasswordResetConfirmation passwordResetConfirmation;

	public PasswordResetController(
		PasswordResetRequest passwordResetRequest,
		PasswordResetVerification passwordResetVerification,
		PasswordResetConfirmation passwordResetConfirmation
	) {
		this.passwordResetRequest = passwordResetRequest;
		this.passwordResetVerification = passwordResetVerification;
		this.passwordResetConfirmation = passwordResetConfirmation;
	}

	@PostMapping("/reset-request")
	public ResponseEntity<Void> requestPasswordReset(
		@Valid @RequestBody PasswordResetRequest.Request request
	) {
		passwordResetRequest.requestReset(request);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/verify-token")
	public ResponseEntity<PasswordResetVerification.Response> verifyResetToken(
		@RequestParam String token
	) {
		PasswordResetVerification.Response response =
			passwordResetVerification.verifyResetToken(new PasswordResetVerification.Request(token));
		return ResponseEntity.ok(response);
	}

	@PostMapping("/reset")
	public ResponseEntity<Void> confirmPasswordReset(
		@Valid @RequestBody PasswordResetConfirmation.Request request
	) {
		passwordResetConfirmation.confirmReset(request);
		return ResponseEntity.ok().build();
	}
}
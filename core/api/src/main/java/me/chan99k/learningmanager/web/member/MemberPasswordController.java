package me.chan99k.learningmanager.web.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset;
import me.chan99k.learningmanager.domain.exception.DomainException;

@RestController
@RequestMapping("/api/v1/members")
public class MemberPasswordController {
	private static final Logger log = LoggerFactory.getLogger(MemberPasswordController.class);

	private final AccountPasswordChange passwordChangeService;
	private final AccountPasswordReset passwordResetService;

	public MemberPasswordController(AccountPasswordChange passwordChangeService,
		AccountPasswordReset passwordResetService) {
		this.passwordChangeService = passwordChangeService;
		this.passwordResetService = passwordResetService;
	}

	@PutMapping("/change-password")
	public ResponseEntity<Void> changePassword(
		@Valid @RequestBody AccountPasswordChange.Request request
	) {
		passwordChangeService.changePassword(request);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping("/reset-password")
	public ResponseEntity<AccountPasswordReset.RequestResetResponse> resetPassword(
		@Valid @RequestBody AccountPasswordReset.RequestResetRequest request
	) {
		// TODO: 프론트엔드 분리 시 응답 구조 표준화
		/*
		 * 현재: 도메인별 응답 객체 사용
		 * 변경 예정: 표준 API 응답 구조
		 */
		AccountPasswordReset.RequestResetResponse response = passwordResetService.requestReset(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/reset-password")
	public ResponseEntity<AccountPasswordReset.TokenVerificationResponse> verifyResetToken(
		@Valid @RequestParam String token
	) {
		try {
			log.info("Password reset token validation started. Token: {}", token);

			// 토큰 검증 및 사용자 정보 반환
			AccountPasswordReset.TokenVerificationResponse response = passwordResetService.verifyResetToken(token);
			log.info("Token validation successful. Token: {}", token);

			return ResponseEntity.ok(response);
		} catch (DomainException e) {
			log.warn("Token validation failed. Token: {}, Error: {}", token, e.getProblemCode().getMessage());

			// 실패 시에도 JSON 응답으로 반환
			AccountPasswordReset.TokenVerificationResponse errorResponse =
				new AccountPasswordReset.TokenVerificationResponse(
					false,
					null,
					token,
					e.getProblemCode().getMessage()
				);
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	@PostMapping("/confirm-reset-password")
	public ResponseEntity<Void> confirmReset(
		@Valid @RequestBody AccountPasswordReset.ConfirmResetRequest request
	) {
		// TODO: 프론트엔드 분리 시 응답 구조 변경 필요
		/*
		 * 현재: 204 No Content (성공 시 빈 응답)
		 * 변경 예정: 성공 정보를 포함한 JSON 응답, 세션 기반 -> JWT 기반
		 */
		passwordResetService.confirmReset(request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}

package me.chan99k.learningmanager.adapter.web.member;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@RestController
@RequestMapping("/api/v1/members")
public class MemberPasswordController {
	private final AccountPasswordChange passwordChangeService;
	private final AccountPasswordReset passwordResetService;
	private final Executor memberTaskExecutor;

	public MemberPasswordController(AccountPasswordChange passwordChangeService,
		AccountPasswordReset passwordResetService, Executor memberTaskExecutor) {
		this.passwordChangeService = passwordChangeService;
		this.passwordResetService = passwordResetService;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@PutMapping("/change-password")
	public CompletableFuture<ResponseEntity<Void>> changePassword(
		@Valid @RequestBody AccountPasswordChange.Request request
	) {
		return CompletableFuture.supplyAsync(() -> {
			passwordChangeService.changePassword(request);

			return ResponseEntity.status(HttpStatus.OK).build();
		}, memberTaskExecutor);
	}

	@PostMapping("/reset-password")
	public CompletableFuture<ResponseEntity<AccountPasswordReset.RequestResetResponse>> resetPassword(
		@Valid @RequestBody AccountPasswordReset.RequestResetRequest request
	) {
		return CompletableFuture.supplyAsync(() -> {
			AccountPasswordReset.RequestResetResponse response = passwordResetService.requestReset(request);

			return ResponseEntity.ok(response);
		}, memberTaskExecutor).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
	}

	@GetMapping("/reset-password")
	public CompletableFuture<ResponseEntity<Void>> getRedirectPage(
		HttpServletRequest request,
		@Valid @RequestParam String token
	) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				// 1. 토큰 유효성 검증
				passwordResetService.validatePasswordResetToken(token);

				// 2. 세션에 검증된 토큰 저장 (보안)
				HttpSession session = request.getSession();
				session.setAttribute("verified_reset_token", token);
				session.setMaxInactiveInterval(300); // 5분 만료

				// 3. 새 비밀번호 입력 페이지로 리다이렉트
				return ResponseEntity.status(HttpStatus.FOUND)
					.location(URI.create("/reset-password-form"))
					.build();
			} catch (DomainException e) {
				// 토큰 유효성 검증 실패 시 에러 페이지로 리다이렉트
				return ResponseEntity.status(HttpStatus.FOUND)
					.location(URI.create("/error?message=invalid_token"))
					.build();
			}
		}, memberTaskExecutor);
	}

	@PostMapping("/confirm-reset-password")
	public CompletableFuture<ResponseEntity<Void>> confirmReset(
		HttpServletRequest httpRequest,
		@Valid @RequestBody AccountPasswordReset.ConfirmResetRequest request
	) {
		return CompletableFuture.supplyAsync(() -> {
			// 세션에서 검증된 토큰 가져오기
			String token = (String)httpRequest.getSession().getAttribute("verified_reset_token");
			if (token == null) {
				throw new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
			}

			passwordResetService.confirmReset(token, request.newPassword());

			// 세션 정리
			httpRequest.getSession().removeAttribute("verified_reset_token");

			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}, memberTaskExecutor);
	}

}

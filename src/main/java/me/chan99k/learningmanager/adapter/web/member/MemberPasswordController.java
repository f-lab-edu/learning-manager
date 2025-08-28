package me.chan99k.learningmanager.adapter.web.member;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;

@RestController
@RequestMapping("/api/v1/members")
public class MemberPasswordController {
	private final AccountPasswordChange passwordChangeService;
	private final Executor memberTaskExecutor;

	public MemberPasswordController(AccountPasswordChange passwordChangeService, Executor memberTaskExecutor) {
		this.passwordChangeService = passwordChangeService;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@PutMapping("/password")
	public CompletableFuture<ResponseEntity<Void>> changePassword(
		@Valid @RequestBody AccountPasswordChange.Request request
	) {
		return CompletableFuture.supplyAsync(() -> {
			passwordChangeService.changePassword(request);

			return ResponseEntity.status(HttpStatus.OK).build();
		}, memberTaskExecutor);
	}
}

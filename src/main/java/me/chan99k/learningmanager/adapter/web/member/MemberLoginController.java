package me.chan99k.learningmanager.adapter.web.member;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.member.provides.MemberLogin;

@RestController
@RequestMapping("/api/v1/members/auth")
public class MemberLoginController {
	private final MemberLogin memberLoginService;
	private final Executor memberTaskExecutor;

	public MemberLoginController(MemberLogin memberLoginService, Executor memberTaskExecutor) {
		this.memberLoginService = memberLoginService;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@PostMapping("/token")
	public CompletableFuture<ResponseEntity<MemberLogin.Response>> login(
		@RequestBody @Valid MemberLogin.Request request
	) {
		return CompletableFuture.supplyAsync(() -> {
			MemberLogin.Response loginResponse = memberLoginService.login(request);
			return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
		}, memberTaskExecutor);
	}
}

package me.chan99k.learningmanager.adapter.web.member;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.member.MemberRegisterService;
import me.chan99k.learningmanager.application.member.provides.MemberRegistration;
import me.chan99k.learningmanager.application.member.provides.SignUpConfirmation;

@RestController
@RequestMapping("/api/v1/members")
public class MemberRegisterController {

	private final MemberRegisterService memberRegisterService;
	private final Executor memberTaskExecutor;

	public MemberRegisterController(MemberRegisterService memberRegisterService, Executor memberTaskExecutor) {
		this.memberRegisterService = memberRegisterService;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@PostMapping("/register")
	public CompletableFuture<ResponseEntity<MemberRegistration.Response>> register(
		@Valid @RequestBody MemberRegistration.Request request
	) {
		return CompletableFuture.supplyAsync(() -> {
				MemberRegistration.Response response = memberRegisterService.register(request);
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
			}, memberTaskExecutor
		);

	}

	@GetMapping("/activate")
	public CompletableFuture<ResponseEntity<Void>> activateMember(
		@RequestParam String token
	) {
		return CompletableFuture.supplyAsync(() -> {
			memberRegisterService.activateSignUpMember(new SignUpConfirmation.Request(token));
			return ResponseEntity.ok().build();
		}, memberTaskExecutor);
	}
}

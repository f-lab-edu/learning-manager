package me.chan99k.learningmanager.adapter.web;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.member.provides.MemberRegistration;

@RestController
@RequestMapping("/api/v1/members")
public class MemberRegisterController {

	private final MemberRegistration memberRegistration;

	public MemberRegisterController(MemberRegistration memberRegistration) {
		this.memberRegistration = memberRegistration;
	}

	@PostMapping("/register")
	public CompletableFuture<ResponseEntity<MemberRegistration.Response>> register(
		@RequestBody MemberRegistration.Request request
	) {
		return CompletableFuture.supplyAsync(() -> {
			MemberRegistration.Response response = memberRegistration.register(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		});
	}

}

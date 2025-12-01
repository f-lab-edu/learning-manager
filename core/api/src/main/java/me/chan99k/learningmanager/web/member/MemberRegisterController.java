package me.chan99k.learningmanager.web.member;

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

	public MemberRegisterController(MemberRegisterService memberRegisterService) {
		this.memberRegisterService = memberRegisterService;
	}

	@PostMapping("/register")
	public ResponseEntity<MemberRegistration.Response> register(
		@Valid @RequestBody MemberRegistration.Request request
	) {
		MemberRegistration.Response response = memberRegisterService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/activate")
	public ResponseEntity<Void> activateMember(
		@RequestParam String token
	) {
		memberRegisterService.activateSignUpMember(new SignUpConfirmation.Request(token));
		return ResponseEntity.ok().build();
	}
}

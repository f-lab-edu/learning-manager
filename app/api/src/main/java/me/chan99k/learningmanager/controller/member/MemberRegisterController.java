package me.chan99k.learningmanager.controller.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.member.MemberRegistration;
import me.chan99k.learningmanager.member.SignUpConfirmation;

@RestController
@RequestMapping("/api/v1/members")
public class MemberRegisterController {

	private final MemberRegistration memberRegistration;
	private final SignUpConfirmation signUpConfirmation;

	public MemberRegisterController(MemberRegistration memberRegistration, SignUpConfirmation signUpConfirmation) {
		this.memberRegistration = memberRegistration;
		this.signUpConfirmation = signUpConfirmation;
	}

	@PostMapping("/register")
	public ResponseEntity<MemberRegistration.Response> register(
		@Valid @RequestBody MemberRegistration.Request request
	) {
		MemberRegistration.Response response = memberRegistration.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/activate")
	public ResponseEntity<Void> activateMember(
		@RequestParam String token
	) {
		signUpConfirmation.activateSignUpMember(new SignUpConfirmation.Request(token));
		return ResponseEntity.ok().build();
	}
}

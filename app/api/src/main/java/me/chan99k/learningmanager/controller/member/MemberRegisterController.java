package me.chan99k.learningmanager.controller.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
	private final String loginUrl;

	public MemberRegisterController(
		MemberRegistration memberRegistration,
		SignUpConfirmation signUpConfirmation,
		@Value("${app.frontend.login-url:/login}") String loginUrl
	) {
		this.memberRegistration = memberRegistration;
		this.signUpConfirmation = signUpConfirmation;
		this.loginUrl = loginUrl;
	}

	@PostMapping("/register")
	public ResponseEntity<MemberRegistration.Response> register(
		@Valid @RequestBody MemberRegistration.Request request
	) {
		MemberRegistration.Response response = memberRegistration.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping(value = "/activate", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> activateMember(@RequestParam String token) {
		signUpConfirmation.activateSignUpMember(new SignUpConfirmation.Request(token));

		String html = """
			<!DOCTYPE html>
			<html>
			<head>
			    <meta charset="UTF-8">
			    <title>이메일 인증 완료</title>
			</head>
			<body>
			    <script>
			        alert('이메일 인증이 완료되었습니다. 로그인 페이지로 이동합니다.');
			        window.location.href = '%s';
			    </script>
			</body>
			</html>
			""".formatted(loginUrl);

		return ResponseEntity.ok().body(html);
	}
}

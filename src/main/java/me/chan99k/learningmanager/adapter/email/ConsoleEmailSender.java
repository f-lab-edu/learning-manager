package me.chan99k.learningmanager.adapter.email;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.EmailSender;

@Component
public class ConsoleEmailSender implements EmailSender {
	@Override
	@Async("emailTaskExecutor")
	public void sendSignUpConfirmEmail(String email, String token) {
		var activateURL = "http://localhost:9999/api/v1/members/activate?token=" + token;

		System.out.println("====== 회원가입 인증 이메일 ======");
		System.out.printf("[System] 회원가입 인증 이메일을 발송하였습니다. 수신자: %s , 활성화 링크: %s , 활성화 토큰: %s\n",
			email, activateURL, token);
		System.out.println("====== 회원가입 인증 이메일 ======");
	}
}

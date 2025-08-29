package me.chan99k.learningmanager.adapter.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.EmailSender;

@Component
public class ConsoleEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

	@Override
	@Async("emailTaskExecutor")
	public void sendSignUpConfirmEmail(String email, String token) {
		var activateURL = "http://localhost:8080/api/v1/members/activate?token=" + token;
		log.info("====== 회원가입 인증 이메일 ======");
		log.info("[System] 회원가입 인증 이메일을 발송하였습니다. 수신자: {}, 활성화 링크: {}", email, activateURL);
		log.info("====== 회원가입 인증 이메일 ======");
	}

	@Override
	public void sendPasswordResetEmail(String email, String token) {
		var resetURL = "http://localhost:8080/api/v1/members/confirm-reset-password?token=" + token;
		log.info("====== 패스워드 초기화 이메일 ======");
		log.info("[System] 패스워드 초기화  이메일을 발송하였습니다. 수신자: {}, 활성화 링크: {}", email, resetURL);
		log.info("====== 패스워드 초기화 이메일 ======");
	}
}

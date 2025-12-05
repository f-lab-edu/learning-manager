package me.chan99k.learningmanager.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.member.EmailSender;

@Component
@Primary
public class AsyncEmailSender implements EmailSender {
	private static final Logger log = LoggerFactory.getLogger(AsyncEmailSender.class);

	private final SmtpEmailSender smtpEmailSender;

	public AsyncEmailSender(SmtpEmailSender smtpEmailSender) {
		this.smtpEmailSender = smtpEmailSender;
	}

	private static void writeStartLog() {
		log.debug("비동기 이메일 발송 시작 - thread: {}", Thread.currentThread().getName());
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendSignUpConfirmEmail(String email, String token) {
		writeStartLog();
		smtpEmailSender.sendSignUpConfirmEmail(email, token);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendPasswordResetEmail(String email, String token) {
		writeStartLog();
		smtpEmailSender.sendPasswordResetEmail(email, token);
	}

}

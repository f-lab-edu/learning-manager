package me.chan99k.learningmanager.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import me.chan99k.learningmanager.member.EmailSender;

@Component
public class SmtpEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

	private final JavaMailSender mailSender;
	private final String fromEmail;
	private final String baseUrl;

	public SmtpEmailSender(
		JavaMailSender mailSender,
		@Value("${email.from}") String fromEmail,
		@Value("${email.base-url}") String baseUrl
	) {
		this.mailSender = mailSender;
		this.fromEmail = fromEmail;
		this.baseUrl = baseUrl;
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendSignUpConfirmEmail(String email, String token) {
		String activateUrl = baseUrl + "/api/v1/members/activate?token=" + token;
		String subject = "[Learning Manager] 회원가입 인증";
		String content = buildSignUpConfirmEmailContent(activateUrl);

		sendEmail(email, subject, content);
		log.info("회원가입 인증 이메일 발송 완료: {}", email);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendPasswordResetEmail(String email, String token) {
		String resetUrl = baseUrl + "/api/v1/members/reset-password?token=" + token;
		String subject = "[Learning Manager] 비밀번호 재설정";
		String content = buildPasswordResetEmailContent(resetUrl);

		sendEmail(email, subject, content);
		log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
	}

	private void sendEmail(String to, String subject, String htmlContent) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlContent, true);  // true = HTML

			mailSender.send(message);
		} catch (MessagingException e) {
			log.error("이메일 발송 실패: to={}, subject={}", to, subject, e);
			throw new RuntimeException("이메일 발송에 실패했습니다.", e);
		}
	}

	private String buildSignUpConfirmEmailContent(String activateUrl) {
		return """
			<!DOCTYPE html>
			<html>
			<head>
			    <meta charset="UTF-8">
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
			    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
			        <h2 style="color: #2c3e50;">Learning Manager 회원가입 인증</h2>
			        <p>안녕하세요!</p>
			        <p>회원가입을 완료하려면 아래 버튼을 클릭해주세요.</p>
			        <div style="text-align: center; margin: 30px 0;">
			            <a href="%s"
			               style="background-color: #3498db; color: white; padding: 12px 30px;
			                      text-decoration: none; border-radius: 5px; display: inline-block;">
			                이메일 인증하기
			            </a>
			        </div>
			        <p style="color: #7f8c8d; font-size: 12px;">
			            이 링크는 24시간 동안 유효합니다.<br>
			            본인이 요청하지 않았다면 이 이메일을 무시해주세요.
			        </p>
			    </div>
			</body>
			</html>
			""".formatted(activateUrl);
	}

	private String buildPasswordResetEmailContent(String resetUrl) {
		return """
			<!DOCTYPE html>
			<html>
			<head>
			    <meta charset="UTF-8">
			</head>
			<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
			    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
			        <h2 style="color: #2c3e50;">비밀번호 재설정</h2>
			        <p>안녕하세요!</p>
			        <p>비밀번호를 재설정하려면 아래 버튼을 클릭해주세요.</p>
			        <div style="text-align: center; margin: 30px 0;">
			            <a href="%s"
			               style="background-color: #e74c3c; color: white; padding: 12px 30px;
			                      text-decoration: none; border-radius: 5px; display: inline-block;">
			                비밀번호 재설정
			            </a>
			        </div>
			        <p style="color: #7f8c8d; font-size: 12px;">
			            이 링크는 24시간 동안 유효합니다.<br>
			            본인이 요청하지 않았다면 이 이메일을 무시해주세요.
			        </p>
			    </div>
			</body>
			</html>
			""".formatted(resetUrl);
	}
}

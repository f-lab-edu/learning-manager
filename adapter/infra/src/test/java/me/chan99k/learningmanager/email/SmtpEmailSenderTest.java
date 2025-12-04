package me.chan99k.learningmanager.email;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_TOKEN = "test-token-123";
	private static final String FROM_EMAIL = "noreply@example.com";
	private static final String BASE_URL = "http://localhost:8080";

	@Mock
	JavaMailSender mailSender;

	@Mock
	MimeMessage mimeMessage;

	SmtpEmailSender emailSender;

	@BeforeEach
	void setUp() {
		given(mailSender.createMimeMessage()).willReturn(mimeMessage);
		emailSender = new SmtpEmailSender(mailSender, FROM_EMAIL, BASE_URL);
	}

	@Nested
	@DisplayName("sendSignUpConfirmEmail 메서드")
	class SendSignUpConfirmEmailTest {

		@Test
		@DisplayName("[Success] 회원가입 인증 이메일을 발송한다")
		void sends_signup_confirm_email() {
			emailSender.sendSignUpConfirmEmail(TEST_EMAIL, TEST_TOKEN);

			then(mailSender).should().createMimeMessage();
			then(mailSender).should().send(mimeMessage);
		}

		@Test
		@DisplayName("[Success] MimeMessage를 생성하고 JavaMailSender로 발송한다")
		void creates_and_sends_mime_message() {
			emailSender.sendSignUpConfirmEmail(TEST_EMAIL, TEST_TOKEN);

			then(mailSender).should(times(1)).createMimeMessage();
			then(mailSender).should(times(1)).send(any(MimeMessage.class));
		}
	}

	@Nested
	@DisplayName("sendPasswordResetEmail 메서드")
	class SendPasswordResetEmailTest {

		@Test
		@DisplayName("[Success] 비밀번호 재설정 이메일을 발송한다")
		void sends_password_reset_email() {
			emailSender.sendPasswordResetEmail(TEST_EMAIL, TEST_TOKEN);

			then(mailSender).should().createMimeMessage();
			then(mailSender).should().send(mimeMessage);
		}
	}

	@Nested
	@DisplayName("이메일 발송 실패")
	class EmailSendFailureTest {

		@Test
		@DisplayName("[Failure] 메일 발송 실패 시 RuntimeException을 던진다")
		void throws_exception_on_send_failure() {
			willThrow(new RuntimeException("SMTP 연결 실패"))
				.given(mailSender).send(any(MimeMessage.class));

			assertThatThrownBy(() -> emailSender.sendSignUpConfirmEmail(TEST_EMAIL, TEST_TOKEN))
				.isInstanceOf(RuntimeException.class);
		}

		@Test
		@DisplayName("[Failure] MimeMessage 생성 실패 시에도 예외가 전파된다")
		void throws_exception_on_message_creation_failure() {
			given(mailSender.createMimeMessage()).willThrow(new RuntimeException("메시지 생성 실패"));

			assertThatThrownBy(() -> emailSender.sendSignUpConfirmEmail(TEST_EMAIL, TEST_TOKEN))
				.isInstanceOf(RuntimeException.class);
		}
	}
}

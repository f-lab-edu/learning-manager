package me.chan99k.learningmanager.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.authentication.PasswordEncoder;
import me.chan99k.learningmanager.authentication.PasswordResetTokenProvider;
import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String RESET_TOKEN = "reset-token-abc123";
	private static final String NEW_PASSWORD = "NewPassword123!";
	private static final String HASHED_PASSWORD = "hashed-new-password";

	@Mock
	MemberQueryRepository memberQueryRepository;
	@Mock
	MemberCommandRepository memberCommandRepository;
	@Mock
	PasswordResetTokenProvider passwordResetTokenProvider;
	@Mock
	PasswordEncoder passwordEncoder;
	@Mock
	EmailSender emailSender;

	PasswordResetService passwordResetService;

	@BeforeEach
	void setUp() {
		passwordResetService = new PasswordResetService(
			memberQueryRepository,
			memberCommandRepository,
			passwordResetTokenProvider,
			passwordEncoder,
			emailSender
		);
	}

	private Member createMemberWithPasswordCredential() {
		Member member = Member.registerDefault(() -> "TestNickname");
		member.addAccount(TEST_EMAIL);
		Account account = member.findAccountByEmail(Email.of(TEST_EMAIL));
		account.addCredential(me.chan99k.learningmanager.member.Credential.ofPassword("old-hashed-password"));
		return member;
	}

	@Nested
	@DisplayName("requestReset 메서드")
	class RequestResetTest {

		@Test
		@DisplayName("가입된 이메일로 비밀번호 재설정을 요청하면 토큰을 생성하고 이메일을 발송한다")
		void creates_token_and_sends_email_for_registered_email() {
			Member member = createMemberWithPasswordCredential();
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordResetTokenProvider.createAndStoreToken(Email.of(TEST_EMAIL)))
				.willReturn(RESET_TOKEN);

			PasswordResetRequest.Request request = new PasswordResetRequest.Request(TEST_EMAIL);

			passwordResetService.requestReset(request);

			then(passwordResetTokenProvider).should().createAndStoreToken(Email.of(TEST_EMAIL));
			then(emailSender).should().sendPasswordResetEmail(TEST_EMAIL, RESET_TOKEN);
		}

		@Test
		@DisplayName("가입되지 않은 이메일로 요청하면 PASSWORD_RESET_EMAIL_NOT_FOUND 예외를 던진다")
		void throws_exception_for_unregistered_email() {
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.empty());

			PasswordResetRequest.Request request = new PasswordResetRequest.Request(TEST_EMAIL);

			assertThatThrownBy(() -> passwordResetService.requestReset(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("verifyResetToken 메서드")
	class VerifyResetTokenTest {

		@Test
		@DisplayName("유효한 토큰이면 true와 이메일을 반환한다")
		void returns_true_and_email_for_valid_token() {
			given(passwordResetTokenProvider.validateResetToken(RESET_TOKEN))
				.willReturn(true);
			given(passwordResetTokenProvider.getEmailFromResetToken(RESET_TOKEN))
				.willReturn(TEST_EMAIL);

			PasswordResetVerification.Request request = new PasswordResetVerification.Request(RESET_TOKEN);

			PasswordResetVerification.Response response = passwordResetService.verifyResetToken(request);

			assertThat(response.tokenValid()).isTrue();
			assertThat(response.email()).isEqualTo(TEST_EMAIL);
		}

		@Test
		@DisplayName("유효하지 않은 토큰이면 false와 null을 반환한다")
		void returns_false_and_null_for_invalid_token() {
			given(passwordResetTokenProvider.validateResetToken(RESET_TOKEN))
				.willReturn(false);

			PasswordResetVerification.Request request = new PasswordResetVerification.Request(RESET_TOKEN);

			PasswordResetVerification.Response response = passwordResetService.verifyResetToken(request);

			assertThat(response.tokenValid()).isFalse();
			assertThat(response.email()).isNull();
		}
	}

	@Nested
	@DisplayName("confirmReset 메서드")
	class ConfirmResetTest {

		@Test
		@DisplayName("유효한 토큰으로 비밀번호를 변경한다")
		void changes_password_with_valid_token() {
			Member member = createMemberWithPasswordCredential();
			given(passwordResetTokenProvider.validateResetToken(RESET_TOKEN))
				.willReturn(true);
			given(passwordResetTokenProvider.getEmailFromResetToken(RESET_TOKEN))
				.willReturn(TEST_EMAIL);
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordEncoder.encode(NEW_PASSWORD))
				.willReturn(HASHED_PASSWORD);
			given(memberCommandRepository.save(member))
				.willReturn(member);

			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request(RESET_TOKEN,
				NEW_PASSWORD);

			passwordResetService.confirmReset(request);

			then(passwordEncoder).should().encode(NEW_PASSWORD);
			then(memberCommandRepository).should().save(member);
			then(passwordResetTokenProvider).should().invalidateAfterUse(RESET_TOKEN);
		}

		@Test
		@DisplayName("유효하지 않은 토큰이면 INVALID_PASSWORD_RESET_TOKEN 예외를 던진다")
		void throws_exception_for_invalid_token() {
			given(passwordResetTokenProvider.validateResetToken(RESET_TOKEN))
				.willReturn(false);

			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request(RESET_TOKEN,
				NEW_PASSWORD);

			assertThatThrownBy(() -> passwordResetService.confirmReset(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		}

		@Test
		@DisplayName("토큰은 유효하지만 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다")
		void throws_exception_when_member_not_found() {
			given(passwordResetTokenProvider.validateResetToken(RESET_TOKEN))
				.willReturn(true);
			given(passwordResetTokenProvider.getEmailFromResetToken(RESET_TOKEN))
				.willReturn(TEST_EMAIL);
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.empty());

			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request(RESET_TOKEN,
				NEW_PASSWORD);

			assertThatThrownBy(() -> passwordResetService.confirmReset(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.MEMBER_NOT_FOUND);
		}

		@Test
		@DisplayName("비밀번호 변경 완료 후 토큰을 무효화한다")
		void invalidates_token_after_password_change() {
			Member member = createMemberWithPasswordCredential();
			given(passwordResetTokenProvider.validateResetToken(RESET_TOKEN))
				.willReturn(true);
			given(passwordResetTokenProvider.getEmailFromResetToken(RESET_TOKEN))
				.willReturn(TEST_EMAIL);
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordEncoder.encode(NEW_PASSWORD))
				.willReturn(HASHED_PASSWORD);
			given(memberCommandRepository.save(member))
				.willReturn(member);

			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request(RESET_TOKEN,
				NEW_PASSWORD);

			passwordResetService.confirmReset(request);

			then(passwordResetTokenProvider).should().invalidateAfterUse(RESET_TOKEN);
		}
	}
}

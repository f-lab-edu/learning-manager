package me.chan99k.learningmanager.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset.ConfirmResetRequest;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset.RequestResetRequest;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset.RequestResetResponse;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.application.member.requires.PasswordResetTokenProvider;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.EmailSender;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	private final String emailString = "test@example.com";
	private final Email email = new Email(emailString);
	private final String token = "reset-token-123";
	private final String newPassword = "NewSecurePass123!";

	@InjectMocks
	private PasswordResetService passwordResetService;

	@Mock
	private MemberQueryRepository memberQueryRepository;
	@Mock
	private MemberCommandRepository memberCommandRepository;
	@Mock
	private PasswordResetTokenProvider passwordResetTokenProvider;
	@Mock
	private EmailSender emailSender;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private Executor emailTaskExecutor;
	@Mock
	private Member member;
	@Mock
	private Account account;

	@Test
	@DisplayName("[Success] 재설정 요청 성공 - 가입된 이메일")
	void requestReset_Success_ExistingEmail() {
		given(memberQueryRepository.findByEmail(email)).willReturn(Optional.of(member));
		given(passwordResetTokenProvider.createResetToken(emailString)).willReturn(token);
		RequestResetRequest request = new RequestResetRequest(emailString);

		RequestResetResponse response = passwordResetService.requestReset(request);

		assertThat(response.message()).contains(emailString);
		verify(passwordResetTokenProvider).createResetToken(emailString);
		verify(emailTaskExecutor).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("[Failure] 재설정 요청 실패 - 가입되지 않은 이메일")
	void requestReset_Failure_NonExistentEmail() {
		given(memberQueryRepository.findByEmail(email)).willReturn(Optional.empty());
		RequestResetRequest request = new RequestResetRequest(emailString);

		assertThatThrownBy(() -> passwordResetService.requestReset(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND);
		verify(passwordResetTokenProvider, never()).createResetToken(anyString());
	}

	@Test
	@DisplayName("[Success] 비밀번호 재설정 확인 성공")
	void confirmReset_Success() {
		ConfirmResetRequest request = new ConfirmResetRequest(token, newPassword);
		given(passwordResetTokenProvider.validateResetToken(token)).willReturn(true);
		given(passwordResetTokenProvider.getEmailFromResetToken(token)).willReturn(email);
		given(member.getAccounts()).willReturn(List.of(account));
		given(member.findAccountByEmail(any(Email.class))).willReturn(account);
		given(account.getId()).willReturn(1L);
		given(memberQueryRepository.findByEmail(any(Email.class))).willReturn(Optional.of(member));

		passwordResetService.confirmReset(request);

		verify(passwordResetTokenProvider).validateResetToken(token);
		verify(passwordResetTokenProvider).getEmailFromResetToken(token);
		verify(member).changeAccountPassword(1L, newPassword, passwordEncoder);
		verify(memberCommandRepository).save(member);
		verify(passwordResetTokenProvider).invalidateAfterUse(token);
	}

	@Test
	@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 유효하지 않은 토큰")
	void confirmReset_Failure_InvalidToken() {
		ConfirmResetRequest request = new ConfirmResetRequest(token, newPassword);
		given(passwordResetTokenProvider.validateResetToken(token)).willReturn(false);

		assertThatThrownBy(() -> passwordResetService.confirmReset(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		verify(passwordResetTokenProvider, never()).getEmailFromResetToken(anyString());
		verify(memberCommandRepository, never()).save(any(Member.class));
		verify(passwordResetTokenProvider, never()).invalidateAfterUse(anyString());
	}

	@Test
	@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 회원 존재하지 않음")
	void confirmReset_Failure_MemberNotFound() {
		ConfirmResetRequest request = new ConfirmResetRequest(token, newPassword);
		given(passwordResetTokenProvider.validateResetToken(token)).willReturn(true);
		given(passwordResetTokenProvider.getEmailFromResetToken(token)).willReturn(email);
		given(memberQueryRepository.findByEmail(email)).willReturn(Optional.empty());

		assertThatThrownBy(() -> passwordResetService.confirmReset(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND);
		verify(passwordResetTokenProvider, never()).invalidateAfterUse(anyString());
	}

	@Test
	@DisplayName("[Success] 토큰 검증 성공")
	void validateToken_Success() {
		given(passwordResetTokenProvider.validateResetToken(token)).willReturn(true);

		boolean result = passwordResetService.validatePasswordResetToken(token);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("[Failure] 토큰 검증 실패")
	void validateToken_Failure() {
		given(passwordResetTokenProvider.validateResetToken(token)).willReturn(false);

		assertThatThrownBy(() -> passwordResetService.validatePasswordResetToken(token))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
	}
}

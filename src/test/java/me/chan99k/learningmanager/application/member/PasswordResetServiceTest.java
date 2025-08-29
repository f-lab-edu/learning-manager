package me.chan99k.learningmanager.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;

import me.chan99k.learningmanager.adapter.auth.PasswordResetTokenManager;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.EmailSender;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	private final String email = "test@example.com";
	private final String token = "reset-token-123";
	private final String newPassword = "NewSecurePass123!";
	@Mock
	private MemberQueryRepository memberQueryRepository;
	@Mock
	private MemberCommandRepository memberCommandRepository;
	@Mock
	private PasswordResetTokenManager passwordResetTokenManager;
	@Mock
	private EmailSender emailSender;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private AsyncTaskExecutor emailTaskExecutor;
	@Mock
	private Member member;
	@Mock
	private Account account;
	@InjectMocks
	private PasswordResetService passwordResetService;

	@Test
	@DisplayName("[Success] 재설정 요청 성공 - 가입된 이메일")
	void requestReset_Success_ExistingEmail() {
		// given
		given(memberQueryRepository.findByEmail(any(Email.class))).willReturn(Optional.of(member));
		given(passwordResetTokenManager.generateAndStoreToken(eq(email), any(Duration.class))).willReturn(token);

		AccountPasswordReset.RequestResetRequest request = new AccountPasswordReset.RequestResetRequest(email);

		// when
		AccountPasswordReset.RequestResetResponse response = passwordResetService.requestReset(request);

		// then
		assertThat(response.message()).contains(email);
		assertThat(response.message()).contains("비밀번호 재설정 메일을 발송했습니다");

		verify(passwordResetTokenManager).generateAndStoreToken(eq(email), any(Duration.class));
		verify(emailTaskExecutor).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("[Failure] 재설정 요청 실패 - 가입되지 않은 이메일")
	void requestReset_Failure_NonExistentEmail() {
		// given
		given(memberQueryRepository.findByEmail(any(Email.class))).willReturn(Optional.empty());

		AccountPasswordReset.RequestResetRequest request = new AccountPasswordReset.RequestResetRequest(email);

		// when & then
		assertThatThrownBy(() -> passwordResetService.requestReset(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND);

		verify(passwordResetTokenManager, never()).generateAndStoreToken(anyString(), any(Duration.class));
		verify(emailTaskExecutor, never()).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("[Success] 비밀번호 재설정 확인 성공")
	void confirmReset_Success() {
		// given
		given(passwordResetTokenManager.getEmailByToken(token)).willReturn(email);
		given(memberQueryRepository.findByEmail(any(Email.class))).willReturn(Optional.of(member));
		given(member.getAccounts()).willReturn(List.of(account));
		given(account.getId()).willReturn(1L);

		// when
		AccountPasswordReset.ConfirmResetResponse response = passwordResetService.confirmReset(token, newPassword);

		// then
		assertThat(response).isNotNull();

		verify(member).changeAccountPassword(1L, newPassword, passwordEncoder);
		verify(passwordResetTokenManager).removeToken(token);
		verify(memberCommandRepository).save(member);
	}

	@Test
	@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 토큰에서 이메일 추출 불가")
	void confirmReset_Failure_InvalidToken() {
		// given
		given(passwordResetTokenManager.getEmailByToken(token)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> passwordResetService.confirmReset(token, newPassword))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);

		verify(passwordResetTokenManager, never()).removeToken(anyString());
		verify(memberCommandRepository, never()).save(any(Member.class));
	}

	@Test
	@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 토큰에서 이메일 추출 불가")
	void confirmReset_Failure_NoEmailFromToken() {
		given(passwordResetTokenManager.getEmailByToken(token)).willReturn(null);

		assertThatThrownBy(() -> passwordResetService.confirmReset(token, newPassword))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
	}

	@Test
	@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 회원 존재하지 않음")
	void confirmReset_Failure_MemberNotFound() {
		given(passwordResetTokenManager.getEmailByToken(token)).willReturn(email);
		given(memberQueryRepository.findByEmail(any(Email.class))).willReturn(Optional.empty());

		assertThatThrownBy(() -> passwordResetService.confirmReset(token, newPassword))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND);
	}

	@Test
	@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 계정 존재하지 않음")
	void confirmReset_Failure_NoAccounts() {
		given(passwordResetTokenManager.getEmailByToken(token)).willReturn(email);
		given(memberQueryRepository.findByEmail(any(Email.class))).willReturn(Optional.of(member));
		given(member.getAccounts()).willReturn(List.of()); // 빈 리스트

		// when & then
		assertThatThrownBy(() -> passwordResetService.confirmReset(token, newPassword))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.ACCOUNT_NOT_FOUND);
	}

	@Test
	@DisplayName("[Success] 토큰 검증 성공")
	void validateToken_Success() {
		// given
		given(passwordResetTokenManager.validateToken(token)).willReturn(true);

		// when
		boolean result = passwordResetService.validatePasswordResetToken(token);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("[Failure] 토큰 검증 실패")
	void validateToken_Failure() {
		// given
		given(passwordResetTokenManager.validateToken(token)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> passwordResetService.validatePasswordResetToken(token))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
	}

}
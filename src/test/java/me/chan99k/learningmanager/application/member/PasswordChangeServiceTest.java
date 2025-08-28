package me.chan99k.learningmanager.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceTest {
	private final Long memberId = 1L;
	private final String email = "test@example.com";
	private final String newPassword = "NewSecurePass456@";
	@Mock
	private MemberQueryRepository memberQueryRepository;
	@Mock
	private MemberCommandRepository memberCommandRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private Member member;
	@Mock
	private Account account;
	@InjectMocks
	private PasswordChangeService passwordChangeService;

	@Test
	@DisplayName("[Success] 유효한 요청의 경우 비밀번호 변경 성공")
	void changePassword_Success() {
		// given
		given(memberQueryRepository.findById(memberId)).willReturn(Optional.of(member));
		given(member.findAccountByEmail(any(Email.class))).willReturn(account);
		given(account.getId()).willReturn(1L);

		AccountPasswordChange.Request request = new AccountPasswordChange.Request(email, newPassword);

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)
		) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			AccountPasswordChange.Response response = passwordChangeService.changePassword(request);

			assertThat(response).isNotNull();
			verify(member).changeAccountPassword(1L, newPassword, passwordEncoder);
			verify(memberCommandRepository).save(member);
		}
	}

	@Test
	@DisplayName("[Failure] 인증 컨텍스트가 없는 경우 예외 발생")
	void changePassword_NoAuthenticationContext() {
		AccountPasswordChange.Request request = new AccountPasswordChange.Request(email, newPassword);

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.empty());

			assertThatThrownBy(() -> passwordChangeService.changePassword(request))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("[System] 인증된 사용자의 컨텍스트를 찾을 수 없습니다");
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 회원인 경우 예외 발생")
	void changePassword_MemberNotFound() {
		given(memberQueryRepository.findById(memberId)).willReturn(Optional.empty());

		AccountPasswordChange.Request request = new AccountPasswordChange.Request(email, newPassword);

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			assertThatThrownBy(() -> passwordChangeService.changePassword(request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 계정인 경우 예외 발생")
	void changePassword_AccountNotFound() {
		// given
		given(memberQueryRepository.findById(memberId)).willReturn(Optional.of(member));
		given(member.findAccountByEmail(any(Email.class)))
			.willThrow(new DomainException(MemberProblemCode.ACCOUNT_NOT_FOUND));

		AccountPasswordChange.Request request = new AccountPasswordChange.Request(email, newPassword);

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			assertThatThrownBy(() -> passwordChangeService.changePassword(request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.ACCOUNT_NOT_FOUND);
		}
	}
}
package me.chan99k.learningmanager.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.application.member.provides.MemberLogin;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.CredentialProvider;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.Password;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberAuthenticationServiceTest {
	private final String testEmail = "test@example.com";
	private final String testPassword = "passW@rd123";
	@Mock
	private MemberQueryRepository memberQueryRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private CredentialProvider credentialProvider;
	@Mock
	private Member member;
	@InjectMocks
	private MemberLoginService memberAuthenticationService;

	@Test
	@DisplayName("올바른 이메일과 패스워드로 로그인 성공")
	void loginSuccess() {
		MemberLogin.Request request = new MemberLogin.Request(testEmail, testPassword);
		var inputEmail = Email.of(testEmail);
		var inputPassword = Password.generatePassword(testPassword, passwordEncoder);

		when(memberQueryRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(member));
		when(member.validateLogin(eq(inputEmail), eq(inputPassword))).thenReturn(true);
		String testToken = "jwt_token_123";
		when(credentialProvider.issueCredential(member)).thenReturn(testToken);

		MemberLogin.Response response = memberAuthenticationService.login(request);

		assertThat(response.accessToken()).isEqualTo(testToken);
		verify(memberQueryRepository).findByEmail(any(Email.class));
		verify(member).validateLogin(inputEmail, inputPassword);
		verify(credentialProvider).issueCredential(member);
	}

	@Test
	@DisplayName("존재하지 않는 이메일로 로그인 실패")
	void loginFailWithInvalidEmail() {
		MemberLogin.Request request = new MemberLogin.Request(testEmail, testPassword);
		when(memberQueryRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

		assertThrows(DomainException.class, () -> {
			memberAuthenticationService.login(request);
		});

		verify(memberQueryRepository).findByEmail(any(Email.class)); // 이메일 값 객체로 호출 했는지 검증
		verifyNoInteractions(member, credentialProvider); // 그 이전에 실패하여 자격 증명 제공자 호출이 없음을 검증
	}

	@Test
	@DisplayName("잘못된 자격증명으로 로그인 실패")
	void loginFailWithInvalidCredentials() {
		MemberLogin.Request request = new MemberLogin.Request(testEmail, testPassword);
		var inputEmail = Email.of(testEmail);
		var inputPassword = Password.generatePassword(testPassword, passwordEncoder);

		when(memberQueryRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(member));
		when(member.validateLogin(eq(inputEmail), eq(inputPassword))).thenReturn(false);

		assertThrows(DomainException.class, () -> {
			memberAuthenticationService.login(request);
		});

		verify(memberQueryRepository).findByEmail(any(Email.class));
		verify(member).validateLogin(inputEmail, inputPassword);
		verifyNoInteractions(credentialProvider); // 그 이전에 실패하여 자격 증명 제공자 호출이 없음을 검증
	}

}
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
import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceTest {

	private static final Long MEMBER_ID = 1L;
	private static final String TEST_EMAIL = "test@example.com";
	private static final String CURRENT_PASSWORD = "CurrentPassword123!";
	private static final String NEW_PASSWORD = "NewPassword456!";
	private static final String HASHED_CURRENT_PASSWORD = "hashed-current-password";
	private static final String HASHED_NEW_PASSWORD = "hashed-new-password";

	@Mock
	MemberQueryRepository memberQueryRepository;
	@Mock
	MemberCommandRepository memberCommandRepository;
	@Mock
	PasswordEncoder passwordEncoder;

	PasswordChangeService passwordChangeService;

	@BeforeEach
	void setUp() {
		passwordChangeService = new PasswordChangeService(
			memberQueryRepository,
			memberCommandRepository,
			passwordEncoder
		);
	}

	private Member createMemberWithPasswordCredential() {
		Member member = Member.registerDefault(() -> "TestNickname");
		member.addAccount(TEST_EMAIL);
		Account account = member.findAccountByEmail(Email.of(TEST_EMAIL));
		account.addCredential(Credential.ofPassword(HASHED_CURRENT_PASSWORD));
		return member;
	}

	@Nested
	@DisplayName("changePassword 메서드")
	class ChangePasswordTest {

		@Test
		@DisplayName("현재 비밀번호가 일치하면 새 비밀번호로 변경한다")
		void changes_password_when_current_password_matches() {
			Member member = createMemberWithPasswordCredential();
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_CURRENT_PASSWORD))
				.willReturn(true);
			given(passwordEncoder.matches(NEW_PASSWORD, HASHED_CURRENT_PASSWORD))
				.willReturn(false);
			given(passwordEncoder.encode(NEW_PASSWORD))
				.willReturn(HASHED_NEW_PASSWORD);
			given(memberCommandRepository.save(member))
				.willReturn(member);

			PasswordChange.Request request = new PasswordChange.Request(CURRENT_PASSWORD, NEW_PASSWORD);

			passwordChangeService.changePassword(MEMBER_ID, request);

			then(passwordEncoder).should().encode(NEW_PASSWORD);
			then(memberCommandRepository).should().save(member);
		}

		@Test
		@DisplayName("회원이 존재하지 않으면 MEMBER_NOT_FOUND 예외를 던진다")
		void throws_exception_when_member_not_found() {
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.empty());

			PasswordChange.Request request = new PasswordChange.Request(CURRENT_PASSWORD, NEW_PASSWORD);

			assertThatThrownBy(() -> passwordChangeService.changePassword(MEMBER_ID, request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.MEMBER_NOT_FOUND);
		}

		@Test
		@DisplayName("현재 비밀번호가 일치하지 않으면 INVALID_CREDENTIAL 예외를 던진다")
		void throws_exception_when_current_password_not_matches() {
			Member member = createMemberWithPasswordCredential();
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_CURRENT_PASSWORD))
				.willReturn(false);

			PasswordChange.Request request = new PasswordChange.Request(CURRENT_PASSWORD, NEW_PASSWORD);

			assertThatThrownBy(() -> passwordChangeService.changePassword(MEMBER_ID, request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.INVALID_CREDENTIAL);
		}

		@Test
		@DisplayName("새 비밀번호가 현재 비밀번호와 동일하면 NEW_PASSWORD_SAME_AS_CURRENT 예외를 던진다")
		void throws_exception_when_new_password_same_as_current() {
			Member member = createMemberWithPasswordCredential();
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_CURRENT_PASSWORD))
				.willReturn(true);
			given(passwordEncoder.matches(NEW_PASSWORD, HASHED_CURRENT_PASSWORD))
				.willReturn(true);

			PasswordChange.Request request = new PasswordChange.Request(CURRENT_PASSWORD, NEW_PASSWORD);

			assertThatThrownBy(() -> passwordChangeService.changePassword(MEMBER_ID, request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.NEW_PASSWORD_SAME_AS_CURRENT);
		}

		@Test
		@DisplayName("비밀번호 변경 후 회원 정보를 저장한다")
		void saves_member_after_password_change() {
			Member member = createMemberWithPasswordCredential();
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_CURRENT_PASSWORD))
				.willReturn(true);
			given(passwordEncoder.matches(NEW_PASSWORD, HASHED_CURRENT_PASSWORD))
				.willReturn(false);
			given(passwordEncoder.encode(NEW_PASSWORD))
				.willReturn(HASHED_NEW_PASSWORD);
			given(memberCommandRepository.save(member))
				.willReturn(member);

			PasswordChange.Request request = new PasswordChange.Request(CURRENT_PASSWORD, NEW_PASSWORD);

			passwordChangeService.changePassword(MEMBER_ID, request);

			then(memberCommandRepository).should().save(member);

			Account account = member.findAccountByEmail(Email.of(TEST_EMAIL));
			Credential credential = account.findCredentialByType(CredentialType.PASSWORD);
			assertThat(credential.getSecret()).isEqualTo(HASHED_NEW_PASSWORD);
		}
	}
}
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

import me.chan99k.learningmanager.auth.PasswordEncoder;
import me.chan99k.learningmanager.auth.SignUpConfirmTokenProvider;
import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class MemberRegisterServiceTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PASSWORD = "Password123!";
	private static final String HASHED_PASSWORD = "hashed-password";
	private static final String CONFIRM_TOKEN = "confirm-token-123";
	private static final Long MEMBER_ID = 1L;

	@Mock
	MemberCommandRepository memberCommandRepository;
	@Mock
	MemberQueryRepository memberQueryRepository;
	@Mock
	NicknameGenerator nicknameGenerator;
	@Mock
	PasswordEncoder passwordEncoder;
	@Mock
	SignUpConfirmTokenProvider signUpConfirmTokenProvider;
	@Mock
	EmailSender emailSender;

	MemberRegisterService memberRegisterService;

	@BeforeEach
	void setUp() {
		memberRegisterService = new MemberRegisterService(
			memberCommandRepository,
			memberQueryRepository,
			nicknameGenerator,
			passwordEncoder,
			signUpConfirmTokenProvider,
			emailSender
		);
	}

	static class TestMember extends Member {
		TestMember() {
			addAccount(TEST_EMAIL);
		}

		@Override
		public Long getId() {
			return MEMBER_ID;
		}
	}

	@Nested
	@DisplayName("register 메서드")
	class RegisterTest {

		@BeforeEach
		void setUpRegister() {
			given(nicknameGenerator.generate()).willReturn("TestUser123");
			given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(HASHED_PASSWORD);
			given(memberCommandRepository.save(any(Member.class)))
				.willAnswer(invocation -> {
					return new TestMember();
				});
			given(signUpConfirmTokenProvider.createAndStoreToken(TEST_EMAIL))
				.willReturn(CONFIRM_TOKEN);
		}

		@Test
		@DisplayName("회원가입 시 확인 토큰을 생성한다")
		void creates_confirm_token() {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			memberRegisterService.register(request);

			then(signUpConfirmTokenProvider).should().createAndStoreToken(TEST_EMAIL);
		}

		@Test
		@DisplayName("회원가입 시 인증 이메일을 발송한다")
		void sends_confirm_email() {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			memberRegisterService.register(request);

			then(emailSender).should().sendSignUpConfirmEmail(TEST_EMAIL, CONFIRM_TOKEN);
		}

		@Test
		@DisplayName("회원가입 시 회원 ID를 반환한다")
		void returns_member_id() {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			MemberRegistration.Response response = memberRegisterService.register(request);

			assertThat(response.memberId()).isEqualTo(MEMBER_ID);
		}

		@Test
		@DisplayName("회원가입 시 비밀번호를 해싱한다")
		void hashes_password() {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			memberRegisterService.register(request);

			then(passwordEncoder).should().encode(TEST_PASSWORD);
		}

		@Test
		@DisplayName("회원가입 시 닉네임을 자동 생성한다")
		void generates_nickname() {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			memberRegisterService.register(request);

			then(nicknameGenerator).should().generate();
		}

		@Test
		@DisplayName("회원가입 시 회원을 저장한다")
		void saves_member() {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			memberRegisterService.register(request);

			then(memberCommandRepository).should().save(any(Member.class));
		}
	}

	@Nested
	@DisplayName("activateSignUpMember 메서드")
	class ActivateSignUpMemberTest {

		private Member createPendingMember() {
			Member member = Member.registerDefault(() -> "TestNickname");
			member.addAccount(TEST_EMAIL);
			return member;
		}

		@Test
		@DisplayName("유효한 토큰으로 회원을 활성화한다")
		void activates_member_with_valid_token() {
			Member member = createPendingMember();
			given(signUpConfirmTokenProvider.validateAndGetEmail(CONFIRM_TOKEN))
				.willReturn(TEST_EMAIL);
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(memberCommandRepository.save(member)).willReturn(member);

			SignUpConfirmation.Request request = new SignUpConfirmation.Request(CONFIRM_TOKEN);

			memberRegisterService.activateSignUpMember(request);

			then(signUpConfirmTokenProvider).should().validateAndGetEmail(CONFIRM_TOKEN);
			then(memberCommandRepository).should().save(member);
			assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		}

		@Test
		@DisplayName("토큰 검증 후 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다")
		void throws_exception_when_member_not_found() {
			given(signUpConfirmTokenProvider.validateAndGetEmail(CONFIRM_TOKEN))
				.willReturn(TEST_EMAIL);
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.empty());

			SignUpConfirmation.Request request = new SignUpConfirmation.Request(CONFIRM_TOKEN);

			assertThatThrownBy(() -> memberRegisterService.activateSignUpMember(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.MEMBER_NOT_FOUND);
		}

		@Test
		@DisplayName("활성화 완료 후 토큰을 삭제한다")
		void removes_token_after_activation() {
			Member member = createPendingMember();
			given(signUpConfirmTokenProvider.validateAndGetEmail(CONFIRM_TOKEN))
				.willReturn(TEST_EMAIL);
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(memberCommandRepository.save(member)).willReturn(member);

			SignUpConfirmation.Request request = new SignUpConfirmation.Request(CONFIRM_TOKEN);
			memberRegisterService.activateSignUpMember(request);

			then(signUpConfirmTokenProvider).should().removeToken(CONFIRM_TOKEN);
		}

		@Test
		@DisplayName("토큰 검증이 실패하면 예외가 전파된다")
		void propagates_exception_when_token_validation_fails() {
			given(signUpConfirmTokenProvider.validateAndGetEmail(CONFIRM_TOKEN))
				.willThrow(new DomainException(MemberProblemCode.INVALID_ACTIVATION_TOKEN));

			SignUpConfirmation.Request request = new SignUpConfirmation.Request(CONFIRM_TOKEN);

			assertThatThrownBy(() -> memberRegisterService.activateSignUpMember(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.INVALID_ACTIVATION_TOKEN);
		}
	}
}

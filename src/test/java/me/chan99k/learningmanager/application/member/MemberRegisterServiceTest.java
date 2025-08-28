package me.chan99k.learningmanager.application.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.chan99k.learningmanager.application.member.provides.MemberRegistration;
import me.chan99k.learningmanager.application.member.provides.SignUpConfirmation;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.AccountStatus;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.EmailSender;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberStatus;
import me.chan99k.learningmanager.domain.member.NicknameGenerator;
import me.chan99k.learningmanager.domain.member.Password;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;
import me.chan99k.learningmanager.domain.member.SignUpConfirmer;
import me.chan99k.learningmanager.domain.member.SystemRole;

@ExtendWith(MockitoExtension.class)
public class MemberRegisterServiceTest {
	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PASSWORD = "testPassword123!";
	private static final String ENCODED_PASSWORD = "ENCODED_PASSWORD";
	private static final String GENERATED_NICKNAME = "generatedNickname";
	private static final String TEST_ACTIVATION_TOKEN = "test-activation-token-123";
	private static final Long MEMBER_ID = 2L;  // 시스템 회원 때문에 2L부터 시작
	private static final Long ACCOUNT_ID = 2L; // 시스템 계정 때문에 2L부터 시작
	@Mock
	private MemberCommandRepository memberCommandRepository;
	@Mock
	private MemberQueryRepository memberQueryRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private NicknameGenerator nicknameGenerator;
	@Mock
	private SignUpConfirmer signUpConfirmer;
	@Mock
	private EmailSender emailSender;
	private MemberRegisterService memberRegisterService;

	@BeforeEach
	void setUp() {
		reset(memberCommandRepository, passwordEncoder, nicknameGenerator, signUpConfirmer, emailSender);

		memberRegisterService = new MemberRegisterService(
			memberCommandRepository,
			memberQueryRepository,
			passwordEncoder,
			nicknameGenerator,
			signUpConfirmer,
			emailSender
		);

	}

	private MemberRegistration.Request createValidRequest() {
		return new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);
	}

	@Nested
	@DisplayName("최초 회원 가입 서비스 테스트")
	class RegisterMemberTest {

		private void setUpSuccessScenario() {
			given(passwordEncoder.encode(TEST_PASSWORD))
				.willReturn(ENCODED_PASSWORD);
			given(nicknameGenerator.generate())
				.willReturn(GENERATED_NICKNAME);
			given(signUpConfirmer.generateAndStoreToken(anyLong(), anyString(), any()))
				.willReturn(TEST_ACTIVATION_TOKEN);
			given(memberCommandRepository.save(any(Member.class)))
				.willAnswer(invocation -> {
					Member member = invocation.getArgument(0);
					ReflectionTestUtils.setField(member, "id", MEMBER_ID);
					return member;
				});
		}

		@Test
		@DisplayName("[Success] 최초 회원가입 과정 에서 협력 객체들과 상호 작용하는데 성공한다.")
		void registerMemberTest01() {
			setUpSuccessScenario();
			MemberRegistration.Request request = createValidRequest();

			MemberRegistration.Response response = memberRegisterService.register(request);

			assertThat(response.memberId()).isEqualTo(MEMBER_ID);

			verify(nicknameGenerator).generate();                  // 1. 닉네임 생성 (Member.registerDefault)
			verify(passwordEncoder).encode(anyString());           // 2. 패스워드 인코딩 (addAccount)
			verify(memberCommandRepository).save(any(Member.class));                  // 3. 회원 저장
			verify(signUpConfirmer).generateAndStoreToken(anyLong(), anyString(), any()); // 4. 토큰 생성
			verify(emailSender).sendSignUpConfirmEmail(anyString(), anyString());         // 5. 이메일 발송
		}

		@Test
		@DisplayName("[Success] 최초 회원가입 과정이 성공적으로 완료되면, 생성된 회원 및 계정이 올바른 초기 상태를 갖는다")
		void registerMemberTest02() {
			setUpSuccessScenario();
			MemberRegistration.Request request = createValidRequest();

			MemberRegistration.Response response = memberRegisterService.register(request);

			assertThat(response.memberId()).isEqualTo(MEMBER_ID);

			ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
			verify(memberCommandRepository).save(memberCaptor.capture());
			Member createdMember = memberCaptor.getValue();

			assertThat(createdMember.getStatus()).isEqualTo(
				MemberStatus.PENDING);    // 새 회원은 PENDING 상태로 시작 (이메일 인증 대기)
			assertThat(createdMember.getRole()).isEqualTo(SystemRole.MEMBER);        // 새 회원은 항상 MEMBER 권한으로 시작
			assertThat(createdMember.getNickname()).isNotNull();                    // 새롭게 회원 가입시 닉네임이 반드시 할당 되어야 함
			assertThat(createdMember.getNickname().value()).isEqualTo(GENERATED_NICKNAME);
			assertThat(createdMember.getAccounts()).hasSize(
				1);            // 새롭게 회원 가입시 정확히 하나의 계정이 생성 되어야 함 (계정이 하나도 없으므로)

			Account account = createdMember.getAccounts().get(0);

			AccountStatus accountStatus = (AccountStatus)ReflectionTestUtils.getField(account, "status");
			Email accountEmail = (Email)ReflectionTestUtils.getField(account, "email");
			Password accountPassword = (Password)ReflectionTestUtils.getField(account, "password");

			// 새롭게 회원 가입하는 회원의 계정은 PENDING 상태로 생성되어야 함
			assertThat(accountStatus).isEqualTo(AccountStatus.PENDING);

			// 회원 가입 요청한 이메일이 정확히 계정에 저장되어야 함
			Assertions.assertNotNull(accountEmail);
			assertThat(accountEmail.address()).isEqualTo(TEST_EMAIL);

			// 패스워드가 인코딩되어 저장되어야 함
			Assertions.assertNotNull(accountPassword);
			assertThat(accountPassword.encoded()).isEqualTo(ENCODED_PASSWORD);
		}

		@Test
		@DisplayName("[Failure] 최초 회원가입 과정에서 닉네임 생성에 실패할 경우, 즉시 예외를 던지고 중단한다")
		void registerMemberTest04() {
			// 닉네임 생성 단계에서 실패
			given(nicknameGenerator.generate())
				.willThrow(new RuntimeException("[System] 닉네임 생성기에서 알수 없는 예외 발생"));

			MemberRegistration.Request request = createValidRequest();

			assertThatThrownBy(() -> memberRegisterService.register(request))
				.isInstanceOf(RuntimeException.class);

			// 후속 작업이 호출되지 않아야 함
			verify(memberCommandRepository, never()).save(any(Member.class));
			verify(signUpConfirmer, never()).generateAndStoreToken(anyLong(), anyString(), any());
			verify(emailSender, never()).sendSignUpConfirmEmail(anyString(), anyString());
		}

		@Test
		@DisplayName("[Failure] 최초 회원가입 과정에서 패스워드 인코딩에 실패할 경우, 즉시 예외를 던지고 중단한다")
		void registerMemberTest03() {
			// 닉네임 생성만 성공 시나리오로 설정 (패스워드 인코딩 전에 실패)
			given(nicknameGenerator.generate()).willReturn(GENERATED_NICKNAME);

			// 패스워드 인코딩 실패 시나리오
			given(passwordEncoder.encode(TEST_PASSWORD))
				.willThrow(new RuntimeException("[System] 패스워드 인코딩 과정에서 알수 없는 예외 발생"));

			MemberRegistration.Request request = createValidRequest();

			assertThatThrownBy(() -> memberRegisterService.register(request))
				.isInstanceOf(RuntimeException.class);

			// 후속 작업이 호출되지 않아야 함
			verify(memberCommandRepository, never()).save(any(Member.class));
			verify(signUpConfirmer, never()).generateAndStoreToken(anyLong(), anyString(), any());
			verify(emailSender, never()).sendSignUpConfirmEmail(anyString(), anyString());
		}

		@Test
		@DisplayName("[Failure] 최초 회원가입 과정에서 데이터베이스 정보 저장에 실패할 경우, 즉시 예외를 던지고 중단한다")
		void registerMemberTest05() {
			given(nicknameGenerator.generate())
				.willReturn(GENERATED_NICKNAME);

			given(passwordEncoder.encode(TEST_PASSWORD))
				.willReturn(ENCODED_PASSWORD);

			// 데이터베이스 저장 실패 시나리오
			given(memberCommandRepository.save(any(Member.class)))
				.willThrow(new RuntimeException("[System] Database에서 알수 없는 예외 발생"));

			MemberRegistration.Request request = createValidRequest();

			assertThatThrownBy(() -> memberRegisterService.register(request))
				.isInstanceOf(RuntimeException.class);

			// 저장 실패 시 후속 작업 중단
			verify(signUpConfirmer, never()).generateAndStoreToken(anyLong(), anyString(), any());
			verify(emailSender, never()).sendSignUpConfirmEmail(anyString(), anyString());
		}

	}

	@Nested
	@DisplayName("회원 가입 시 이메일 인증 및 활성화 과정 테스트")
	class ActivateSignUpMemberTest {
		@AfterEach
		void tearDownMocks() {
			clearInvocations(signUpConfirmer, memberCommandRepository);
		}

		@Test
		@DisplayName("[Success] 이메일 인증 및 활성화 과정에서 협력 객체들과 상호작용한다")
		void activateSignUpMemberTest01() {
			Member pendingMember = createMemberWithPendingAccount();

			given(signUpConfirmer.getMemberIdByToken(TEST_ACTIVATION_TOKEN)).willReturn(MEMBER_ID);
			given(signUpConfirmer.validateToken(TEST_ACTIVATION_TOKEN)).willReturn(true);
			given(memberQueryRepository.findById(MEMBER_ID)).willReturn(Optional.of(pendingMember));

			memberRegisterService.activateSignUpMember(new SignUpConfirmation.Request(TEST_ACTIVATION_TOKEN));

			verify(signUpConfirmer).getMemberIdByToken(TEST_ACTIVATION_TOKEN);
			verify(memberQueryRepository).findById(MEMBER_ID);
			verify(memberCommandRepository).save(pendingMember);
			verify(signUpConfirmer).removeToken(TEST_ACTIVATION_TOKEN);
		}

		@Test
		@DisplayName("[Success] 회원 이메일 인증 과정이 성공적으로 끝났을 때, 회원과 계정 상태가 모두 활성화된다")
		void activateSignUpMemberTest02() {
			given(signUpConfirmer.validateToken(TEST_ACTIVATION_TOKEN)).willReturn(true);
			given(signUpConfirmer.getMemberIdByToken(TEST_ACTIVATION_TOKEN)).willReturn(MEMBER_ID);
			Member pendingMember = createMemberWithPendingAccount();
			given(memberQueryRepository.findById(MEMBER_ID)).willReturn(Optional.of(pendingMember));

			assertThat(pendingMember.getStatus()).isEqualTo(MemberStatus.PENDING);
			Account account = pendingMember.getAccounts().get(0);

			AccountStatus initialStatus = (AccountStatus)ReflectionTestUtils.getField(account, "status");
			assertThat(initialStatus).isEqualTo(AccountStatus.PENDING);

			memberRegisterService.activateSignUpMember(new SignUpConfirmation.Request(TEST_ACTIVATION_TOKEN));

			assertThat(pendingMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);

			AccountStatus activatedStatus = (AccountStatus)ReflectionTestUtils.getField(account, "status");
			assertThat(activatedStatus).isEqualTo(AccountStatus.ACTIVE);
		}

		@Test
		@DisplayName("[Failure] 회원 이메일 인증 과정에서 유효하지 않은 토큰일 경우, 즉시 예외를 던지고 중단한다")
			// 유효하지 않은 토큰이 무엇인지 명확하지 않음
		void activateSignUpMemberTest03() {
			given(signUpConfirmer.validateToken("invalid-token")).willReturn(true);
			given(signUpConfirmer.getMemberIdByToken("invalid-token")).willReturn(
				null); // TODO :: NULL 반환을 로직의 일부로 취급하지 않도록 로직 변경 필요 -> Optional 반환으로 인터페이스 변경

			assertThatThrownBy(
				() -> memberRegisterService.activateSignUpMember(new SignUpConfirmation.Request("invalid-token")))
				.isInstanceOf(DomainException.class)
				.hasMessage(INVALID_ACTIVATION_TOKEN.getMessage());

			verify(memberQueryRepository, never()).findById(anyLong());
			verify(memberCommandRepository, never()).save(any(Member.class));
			verify(signUpConfirmer, never()).removeToken(anyString());
		}

		@Test
		@DisplayName("[Failure] 회원 이메일 인증 과정에서 토큰이 만료된 경우, 즉시 예외를 던지고 중단한다")
		void activateSignUpMemberTestTimeout() {
			given(signUpConfirmer.validateToken("timeout-token")).willThrow(
				new DomainException(EXPIRED_ACTIVATION_TOKEN));

			assertThatThrownBy(
				() -> memberRegisterService.activateSignUpMember(new SignUpConfirmation.Request("timeout-token")))
				.isInstanceOf(DomainException.class)
				.hasMessage(EXPIRED_ACTIVATION_TOKEN.getMessage());

			verify(memberQueryRepository, never()).findById(anyLong());
			verify(memberCommandRepository, never()).save(any(Member.class));
			verify(signUpConfirmer, never()).removeToken(anyString());
		}

		@Test
		@DisplayName("[Failure] 회원 이메일 인증 과정에서 존재하지 않는 회원일 경우, 즉시 예외를 던지고 중단한다")
		void activateSignUpMemberTest04() {
			given(signUpConfirmer.validateToken(TEST_ACTIVATION_TOKEN)).willReturn(true);
			given(signUpConfirmer.getMemberIdByToken(TEST_ACTIVATION_TOKEN)).willReturn(999L);
			given(memberQueryRepository.findById(999L)).willReturn(Optional.empty());

			assertThatThrownBy(() -> memberRegisterService.activateSignUpMember(new SignUpConfirmation.Request(
				TEST_ACTIVATION_TOKEN)))
				.isInstanceOf(DomainException.class)
				.hasMessage(MEMBER_NOT_FOUND.getMessage());

			verify(memberCommandRepository, never()).save(any(Member.class));
			verify(signUpConfirmer, never()).removeToken(TEST_ACTIVATION_TOKEN);
		}

		private Member createMemberWithPendingAccount() {
			Member member = Member.registerDefault(() -> GENERATED_NICKNAME);
			ReflectionTestUtils.setField(member, "id", MEMBER_ID);
			member.addAccount(TEST_EMAIL, TEST_PASSWORD, new PasswordEncoder() {
				@Override
				public String encode(String rawString) {
					return ENCODED_PASSWORD;
				}

				@Override
				public boolean matches(String rawString, String encoded) {
					return rawString.equals(ENCODED_PASSWORD);
				}
			});

			Account account = member.getAccounts().get(0);
			ReflectionTestUtils.setField(account, "id", ACCOUNT_ID);

			return member;
		}

	}
}

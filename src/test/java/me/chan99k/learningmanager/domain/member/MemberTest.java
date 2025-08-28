package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MemberTest {

	private PasswordEncoder passwordEncoder;
	private Member member;

	private Member createTestMember() {
		Member testMember = new Member();

		ReflectionTestUtils.setField(testMember, "nickname", new Nickname("testuser"));
		ReflectionTestUtils.setField(testMember, "role", SystemRole.MEMBER);
		ReflectionTestUtils.setField(testMember, "status", MemberStatus.ACTIVE);
		ReflectionTestUtils.setField(testMember, "accounts", new ArrayList<>());
		return testMember;
	}

	@BeforeEach
	void setUp() {
		this.passwordEncoder = new PasswordEncoder() {
			@Override
			public String encode(String rawString) {
				return rawString.toUpperCase();
			}

			@Override
			public boolean matches(String rawString, String encoded) {
				return encoded.equals(encode(rawString));
			}
		};
		member = createTestMember();
	}

	@Nested
	@DisplayName("회원 관리 테스트")
	class MemberCreationTest {
		private NicknameGenerator stubGenerator;

		@BeforeEach
		void setUp() {
			stubGenerator = () -> "defaultUser";
		}

		@Test
		@DisplayName("[Success] 회원 ID 반환에 성공한다.")
		void success_to_return_memberId() {
			ReflectionTestUtils.setField(member, "id", 1L);
			assertThat(member.getId()).isNotNull();
			assertThat(member.getId()).isEqualTo(1L);
		}

		@Test
		@DisplayName("[Success] 프로필 업데이트에 성공한다.")
		void success_to_update_profile() {
			String updatingUrl = "https://updated.com/profile.jpg";
			String updatingIntro = "안녕하세요, 이것은 수정된 자기소개입니다.";

			member.updateProfile(updatingUrl, updatingIntro);
			assertThat(member.getProfileImageUrl()).isEqualTo(updatingUrl);
			assertThat(member.getSelfIntroduction()).isEqualTo(updatingIntro);
		}

		@Test
		@DisplayName("[Success] 닉네임 생성기를 통해 기본 회원 생성에 성공한다")
		void success_to_register_default_member() {
			Member defaultMember = Member.registerDefault(stubGenerator);

			assertThat(defaultMember).isNotNull();
			assertThat(defaultMember.getNickname().value()).isEqualTo("defaultUser");
			assertThat(defaultMember.getRole()).isEqualTo(SystemRole.MEMBER);
			assertThat(defaultMember.getStatus()).isEqualTo(MemberStatus.PENDING);
		}
	}

	@Nested
	@DisplayName("회원 닉네임 테스트")
	class NicknameTest {
		@Test
		@DisplayName("[Success] 회원 닉네임 변경에 성공한다")
		void update_member_nickname() {
			assertThat(member.getNickname().value()).isEqualTo("testuser");

			Nickname newNickname = new Nickname("newnickname");

			member.changeNickname(newNickname);

			assertThat(member.getNickname()).isEqualTo(newNickname);
		}
	}

	@Nested
	@DisplayName("회원 상태 테스트")
	class StatusTest {
		@Test
		@DisplayName("[Failure] 활동 중이 아닌 회원을 이용 정지시키려 하면 예외가 발생한다")
		void ban_fails_if_not_active() {
			member.deactivate(); // 활동 중이 아닌 상태로 만듦

			assertThatThrownBy(member::ban)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_NOT_ACTIVE.getMessage());
		}

		@Test
		@DisplayName("[Failure] 휴면 상태가 아닌 회원을 활성화시키려 하면 예외가 발생한다")
		void activate_fails_if_not_inactive() {
			assertThatThrownBy(member::activate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_NOT_PENDING_OR_INACTIVE.getMessage());

			member.ban(); // BANNED 상태로 변경

			assertThatThrownBy(member::activate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_NOT_PENDING_OR_INACTIVE.getMessage());
		}

		@Test
		@DisplayName("[Success] 휴면 상태인 회원을 활성화하는데 성공한다")
		void success_to_activate_member() {
			member.deactivate(); // 휴면 상태로 변경
			assertThat(member.getStatus()).isEqualTo(MemberStatus.INACTIVE);

			member.activate();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		}

		@Test
		@DisplayName("[Success] 회원 탈퇴에 성공한다.")
		void success_to_withdraw() {
			member.withdraw();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
		}

		@Test
		@DisplayName("[Success] 회원의 밴을 풀어 주는 것에 성공한다.")
		void success_to_unban_banned_user() {
			member.ban();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.BANNED);
			member.unban();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		}

		@Test
		@DisplayName("[Failure] 이미 휴면 상태인 회원을 다시 휴면시키려 하면 예외가 발생한다.")
		void deactivate_fails_if_already_inactive() {
			member.deactivate(); // 휴면 상태로 변경

			assertThatThrownBy(member::deactivate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_ALREADY_INACTIVE.getMessage());
		}

		@Test
		@DisplayName("[Failure] 이미 탈퇴한 회원을 다시 탈퇴시키려 하면 예외가 발생한다.")
		void withdraw_fails_if_already_withdrawn() {
			member.withdraw(); // 탈퇴 상태로 변경

			assertThatThrownBy(member::withdraw)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_ALREADY_WITHDRAWN.getMessage());
		}

		@Test
		@DisplayName("[Failure] 이용 정지 상태가 아닌 회원의 정지를 해제하려 하면 예외가 발생한다.")
		void unban_fails_if_not_banned() {
			// 기본 상태(ACTIVE)에서 unban 시도
			assertThatThrownBy(member::unban)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_NOT_BANNED.getMessage());
		}
	}

	@Nested
	@DisplayName("회원 권한 테스트")
	class RoleTest {
		@Test
		@DisplayName("[Success] 관리자 승급에 성공한다.")
		void success_to_promote_admin() {
			member.promoteToAdmin();
			assertThat(member.getRole()).isEqualTo(SystemRole.ADMIN);
		}

		@Test
		@DisplayName("[Failure] 이미 관리자인 경우, 관리자 승급에 실패한다.")
		void fail_to_promote_admin() {
			member.promoteToAdmin();
			assertThat(member.getRole()).isEqualTo(SystemRole.ADMIN);
			assertThatThrownBy(() -> member.promoteToAdmin()).isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_NOT_GENERAL.getMessage());
		}

		@Test
		@DisplayName("[Success] 회원등급 강등에 성공한다.")
		void success_to_demote_member() {
			member.promoteToAdmin();
			assertThat(member.getRole()).isEqualTo(SystemRole.ADMIN);

			member.demoteToMember();
			assertThat(member.getRole()).isEqualTo(SystemRole.MEMBER);
		}

		@Test
		@DisplayName("[Failure] 더 강등할 등급이 없는 경우, 회원 등급 강등에 실패한다..")
		void fail_to_demote_member() {
			assertThatThrownBy(() -> member.demoteToMember()).isInstanceOf(IllegalStateException.class)
				.hasMessage(MEMBER_NOT_ADMIN.getMessage());
		}
	}

	@Nested
	@DisplayName("계정 정보 관리 테스트")
	class AccountManagementTest {

		private Long accountId;

		@BeforeEach
		void setUp() {
			member.addAccount("chan99k@example.com", "deprecatingPassword123!", passwordEncoder);

			List<Account> accounts = (List<Account>)ReflectionTestUtils.getField(member, "accounts");
			Account addedAccount = accounts.get(0);
			accountId = 1L;
			ReflectionTestUtils.setField(addedAccount, "id", accountId);
		}

		@Test
		@DisplayName("[Success] 기존 비밀번호를 새로운 비밀번호로 바꾸는 데 성공한다.")
		void success_to_changePassword() {
			Account account = member.findAccountById(accountId);
			String oldEncodedPassword = account.getPassword().encoded();
			String newRawPassword = "newPassword123!";

			member.changeAccountPassword(accountId, newRawPassword, passwordEncoder);

			Account updatedAccount = member.findAccountById(accountId);

			assertThat(updatedAccount.getPassword().encoded()).isNotEqualTo(oldEncodedPassword);
			assertThat(passwordEncoder.matches(newRawPassword, updatedAccount.getPassword().encoded())).isTrue();
		}

		@Test
		@DisplayName("[Success] 계정 사용을 활성화하는데 성공한다.")
		void success_to_activate_account() {
			Account account = member.findAccountById(accountId);
			assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);

			member.activateAccount(accountId);

			Account updatedAccount = member.findAccountById(accountId);

			assertThat(updatedAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		}

		@Test
		@DisplayName("[Failure] 활성 상태의 계정이라면 활성화에 실패한다.")
		void fail_to_activate_account_when_status_is_not_pending_or_inactive() {
			member.activateAccount(accountId);

			assertThatThrownBy(() -> member.activateAccount(accountId))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(ACCOUNT_NOT_PENDING_OR_INACTIVE.getMessage());
		}

		@Test
		@DisplayName("[Success] 계정을 비활성화 하는데 성공한다.")
		void success_to_deactivate_account() {
			member.activateAccount(accountId);
			Account account = member.findAccountById(accountId);
			assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);

			member.deactivateAccount(accountId);

			Account updatedAccount = member.findAccountById(accountId);

			assertThat(updatedAccount.getStatus()).isEqualTo(AccountStatus.INACTIVE);
		}

		@Test
		@DisplayName("[Failure] 활성 상태의 계정이 아니라면 비활성화에 실패한다.")
		void fail_to_deactivate_account() {
			assertThatThrownBy(() -> member.deactivateAccount(accountId))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(ACCOUNT_NOT_ACTIVE.getMessage());
		}
	}
}
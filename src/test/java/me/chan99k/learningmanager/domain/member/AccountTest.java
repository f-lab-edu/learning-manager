package me.chan99k.learningmanager.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AccountTest {
	private PasswordEncoder passwordEncoder;
	private Member member;

	@BeforeEach
	void setUp() {
		this.passwordEncoder = new PasswordEncoder() {
			@Override
			public String encode(String rawString) {
				return rawString.toUpperCase();
			}

			@Override
			public boolean match(String rawString, String encoded) {
				return encoded.equals(encode(rawString));
			}
		};
		this.member = Member.registerDefault(() -> "test_" + Instant.now().toEpochMilli());

	}

	@Nested
	@DisplayName("계정 생성 테스트")
	class AccountCreationTest {
		@Test
		@DisplayName("[Success] 올바른 정보로 계정을 생성하는 데 성공한다")
		void create_account_succeeds() {
			Account account = Account.create(member, "test@example.com", "ValidPass123!", passwordEncoder);

			assertThat(account.getMember()).isEqualTo(member);
			assertThat(account.getEmail().address()).isEqualTo("test@example.com");
			assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);
		}

		@Test
		@DisplayName("[Failure] memberId 없이 account를 생성하려 하면 예외가 발생한다")
		void create_account_with_null_memberId_throws_exception() {
			assertThatThrownBy(() -> Account.create(null, "test@example.com", "ValidPass123!", passwordEncoder))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 계정은 반드시 멤버에 속해야 합니다.");
		}

	}

	@Nested
	@DisplayName("계정 정보 관리 테스트")
	class AccountManagementTest {
		Account account;

		@BeforeEach
		void setUp() {
			account = Account.create(member, "chan99k@example.com", "deprecatingPassword123!", passwordEncoder);
		}

		@Test
		@DisplayName("[Success] 기존 비밀번호를 새로운 비밀번호로 바꾸는 데 성공한다.")
		void success_to_changePassword() {
			String oldPassword = account.getPassword().encoded();
			String newPassword = "newPassword123!";

			account.changePassword(newPassword, passwordEncoder);

			assertThat(account.getPassword().encoded()).isNotEqualTo(oldPassword);

		}

		@Test
		@DisplayName("[Success] 계정 사용을 활성화하는데 성공한다.")
		void success_to_activate_account() {
			assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);

			account.activate();

			assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		}

		@Test
		@DisplayName("[Failure] 활성 상태의 계정이라면 활성화에 실패한다.")
		void fail_to_activate_account_when_status_is_not_pending_or_inactive() {
			account.activate();

			assertThatThrownBy(account::activate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("[System] 활성 대기/비활성 상태의 계정이 아닙니다.");
		}

		@Test
		@DisplayName("[Failure] 비활성 상태의 계정이라면 활성화에 실패한다.")
		void fail_to_activate_account_when_status_is_not_inactive() {
			account.activate();

			assertThatThrownBy(account::activate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("[System] 활성 대기/비활성 상태의 계정이 아닙니다.");
		}

		@Test
		@DisplayName("[Success] 계정을 비활성화 하는데 성공한다.")
		void success_to_deactivate_account() {
			account.activate();

			account.deactivate();

			assertThat(account.getStatus()).isEqualTo(AccountStatus.INACTIVE);
		}

		@Test
		@DisplayName("[Failure] 활성 상태의 계정이 아니라면 비활성화에 실패한다.")
		void fail_to_deactivate_account() {

			assertThatThrownBy(account::deactivate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("[System] 활성 상태의 계정이 아닙니다.");
		}
	}

}
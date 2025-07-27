package me.chan99k.learningmanager.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
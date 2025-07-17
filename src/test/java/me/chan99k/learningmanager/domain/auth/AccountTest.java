package me.chan99k.learningmanager.domain.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {
    private PasswordEncoder passwordEncoder;

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
    }

    @Test
    @DisplayName("올바른 정보로 계정을 생성하는 데 성공한다")
    void create_account_succeeds() {
        // given
        Long memberId = 1L;
        CreateAccountRequest request = new CreateAccountRequest("test@example.com", "ValidPass123!", memberId);

        // when
        Account account = Account.create(request, passwordEncoder);

        // then
        assertThat(account.getMemberId()).isEqualTo(memberId);
        assertThat(account.getEmail().address()).isEqualTo("test@example.com");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);
    }

    @Test
    @DisplayName("memberId 없이 계정을 생성하려 하면 예외가 발생한다")
    void create_account_with_null_memberId_throws_exception() {
        // given
        CreateAccountRequest request = new CreateAccountRequest("test@example.com", "ValidPass123!", null);

        // when & then
        assertThatThrownBy(() -> Account.create(request, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정은 반드시 멤버에 속해야 합니다.");
    }

    // 여기에 Account의 다른 상태 변경 테스트(activate, deactivate 등)를 추가할 수 있습니다.
}
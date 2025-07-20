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
    @DisplayName("[Success] 올바른 정보로 계정을 생성하는 데 성공한다")
    void create_account_succeeds() {

        Long memberId = 1L;
        CreateAccountRequest request = new CreateAccountRequest("test@example.com", "ValidPass123!", memberId);


        Account account = Account.create(request, passwordEncoder);


        assertThat(account.getMemberId()).isEqualTo(memberId);
        assertThat(account.getEmail().address()).isEqualTo("test@example.com");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);
    }

    @Test
    @DisplayName("[Failure] memberId 없이 계정을 생성하려 하면 예외가 발생한다")
    void create_account_with_null_memberId_throws_exception() {

        CreateAccountRequest request = new CreateAccountRequest("test@example.com", "ValidPass123!", null);


        assertThatThrownBy(() -> Account.create(request, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정은 반드시 멤버에 속해야 합니다.");
    }

}
package me.chan99k.learningmanager.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoder() {
        @Override
        public String encode(String rawString) {
            return new StringBuilder(rawString).reverse().toString();
        }
        @Override
        public boolean match(String rawString, String encoded) {
            return encoded.equals(encode(rawString));
        }
    };

    @Test
    @DisplayName("올바른 형식의 비밀번호로 객체를 생성하는 데 성공한다")
    void create_password_with_valid_format() {
        assertThatCode(() -> Password.generatePassword("ValidPass1!", passwordEncoder)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1!", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}) // 7자, 65자
    @DisplayName("비밀번호 길이가 유효하지 않으면 예외가 발생한다")
    void password_length_validation(String invalidPassword) {
        assertThatThrownBy(() -> Password.generatePassword(invalidPassword, passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호는 최소 8자 이상, 64자 이하여야 합니다.");
    }

    @Test
    @DisplayName("소문자가 없으면 예외가 발생한다")
    void password_requires_lowercase() {
        assertThatThrownBy(() -> Password.generatePassword("VALIDPASS1!", passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호에는 소문자가 최소 1개 이상 포함되어야 합니다.");
    }

    @Test
    @DisplayName("대문자가 없으면 예외가 발생한다")
    void password_requires_uppercase() {
        assertThatThrownBy(() -> Password.generatePassword("validpass1!", passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호에는 대문자가 최소 1개 이상 포함되어야 합니다.");
    }

    @Test
    @DisplayName("숫자가 없으면 예외가 발생한다")
    void password_requires_digit() {
        assertThatThrownBy(() -> Password.generatePassword("ValidPass!", passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호에는 숫자가 최소 1개 이상 포함되어야 합니다.");
    }

    @Test
    @DisplayName("특수문자가 없으면 예외가 발생한다")
    void password_requires_special_char() {
        assertThatThrownBy(() -> Password.generatePassword("ValidPass1", passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호에는 특수문자가 최소 1개 이상 포함되어야 합니다.");
    }

    @Test
    @DisplayName("공백이 포함되면 예외가 발생한다")
    void password_cannot_contain_whitespace() {
        assertThatThrownBy(() -> Password.generatePassword("Valid Pass 1!", passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호에 공백을 포함할 수 없습니다.");
    }
}

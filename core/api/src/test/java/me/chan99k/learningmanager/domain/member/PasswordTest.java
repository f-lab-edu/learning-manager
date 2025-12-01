package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

	private final PasswordEncoder passwordEncoder = new PasswordEncoder() {
		@Override
		public String encode(String rawString) {
			return new StringBuilder(rawString).reverse().toString();
		}

		@Override
		public boolean matches(String rawString, String encoded) {
			return encoded.equals(encode(rawString));
		}
	};

	@Test
	@DisplayName("[Success] 올바른 형식의 비밀번호로 객체를 생성하는 데 성공한다")
	void create_password_with_valid_format() {
		assertThatCode(() -> Password.generatePassword("ValidPass1!", passwordEncoder)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("[Success] 올바른 비밀번호로 검증 시 true를 반환한다")
	void verify_success_with_correct_password() {
		String rawPassword = "CorrectPassword1!";
		Password password = Password.generatePassword(rawPassword, passwordEncoder);

		boolean result = password.matches(rawPassword, passwordEncoder);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("[Failure] 틀린 비밀번호로 검증 시 false를 반환한다")
	void verify_failure_with_incorrect_password() {
		String rawPassword = "CorrectPassword1!";
		String wrongPassword = "WrongPassword1!";
		Password password = Password.generatePassword(rawPassword, passwordEncoder);

		boolean result = password.matches(wrongPassword, passwordEncoder);

		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"short1!", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}) // 7자, 65자
	@DisplayName("[Failure] 비밀번호 길이가 유효하지 않으면 예외가 발생한다")
	void password_length_validation(String invalidPassword) {
		assertThatThrownBy(() -> Password.generatePassword(invalidPassword, passwordEncoder))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(PASSWORD_LENGTH_INVALID.getMessage());
	}

	@Test
	@DisplayName("[Failure] 소문자가 없으면 예외가 발생한다")
	void password_requires_lowercase() {
		assertThatThrownBy(() -> Password.generatePassword("VALIDPASS1!", passwordEncoder))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(PASSWORD_NO_LOWERCASE.getMessage());
	}

	@Test
	@DisplayName("[Failure] 대문자가 없으면 예외가 발생한다")
	void password_requires_uppercase() {
		assertThatThrownBy(() -> Password.generatePassword("validpass1!", passwordEncoder))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(PASSWORD_NO_UPPERCASE.getMessage());
	}

	@Test
	@DisplayName("[Failure] 숫자가 없으면 예외가 발생한다")
	void password_requires_digit() {
		assertThatThrownBy(() -> Password.generatePassword("ValidPass!", passwordEncoder))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(PASSWORD_NO_DIGIT.getMessage());
	}

	@Test
	@DisplayName("[Failure] 특수문자가 없으면 예외가 발생한다")
	void password_requires_special_char() {
		assertThatThrownBy(() -> Password.generatePassword("ValidPass1", passwordEncoder))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(PASSWORD_NO_SPECIAL_CHAR.getMessage());
	}

	@Test
	@DisplayName("[Failure] 공백이 포함되면 예외가 발생한다")
	void password_cannot_contain_whitespace() {
		assertThatThrownBy(() -> Password.generatePassword("Valid Pass 1!", passwordEncoder))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(PASSWORD_CONTAINS_WHITESPACE.getMessage());
	}
}

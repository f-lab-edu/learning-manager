package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

	@Test
	@DisplayName("[Success] 올바른 형식의 이메일로 객체를 생성하는 데 성공한다")
	void create_email_with_valid_address() {
		assertThatCode(() -> new Email("test@example.com")).doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "invalid-email", "test@.com", "test@domain."})
	@DisplayName("[Failure] 잘못된 형식의 이메일로 객체를 생성하려 하면 예외가 발생한다")
	void create_email_with_invalid_address_throws_exception(String invalidEmail) {
		assertThatThrownBy(() -> new Email(invalidEmail))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage(INVALID_EMAIL_FORMAT.getMessage());
	}
}

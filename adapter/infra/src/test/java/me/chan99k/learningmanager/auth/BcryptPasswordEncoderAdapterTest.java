package me.chan99k.learningmanager.auth;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BcryptPasswordEncoderAdapterTest {

	private static final String RAW_PASSWORD = "Password123!";
	private static final String WRONG_PASSWORD = "WrongPassword123!";

	BcryptPasswordEncoderAdapter passwordEncoder;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BcryptPasswordEncoderAdapter();
	}

	@Nested
	@DisplayName("encode 메서드")
	class EncodeTest {

		@Test
		@DisplayName("비밀번호를 해시한다")
		void encodes_password() {
			String encoded = passwordEncoder.encode(RAW_PASSWORD);

			assertThat(encoded).isNotNull();
			assertThat(encoded).isNotEqualTo(RAW_PASSWORD);
		}

		@Test
		@DisplayName("같은 비밀번호도 매번 다른 해시를 생성한다")
		void generates_different_hash_for_same_password() {
			String encoded1 = passwordEncoder.encode(RAW_PASSWORD);
			String encoded2 = passwordEncoder.encode(RAW_PASSWORD);

			assertThat(encoded1).isNotEqualTo(encoded2);
		}

		@Test
		@DisplayName("해시된 비밀번호는 BCrypt 형식이다")
		void encoded_password_is_bcrypt_format() {
			String encoded = passwordEncoder.encode(RAW_PASSWORD);

			assertThat(encoded).startsWith("$2");
		}
	}

	@Nested
	@DisplayName("matches 메서드")
	class MatchesTest {

		@Test
		@DisplayName("올바른 비밀번호는 true를 반환한다")
		void returns_true_for_correct_password() {
			String encoded = passwordEncoder.encode(RAW_PASSWORD);

			boolean result = passwordEncoder.matches(RAW_PASSWORD, encoded);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("틀린 비밀번호는 false를 반환한다")
		void returns_false_for_wrong_password() {
			String encoded = passwordEncoder.encode(RAW_PASSWORD);

			boolean result = passwordEncoder.matches(WRONG_PASSWORD, encoded);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("다른 해시에서도 같은 비밀번호는 일치한다")
		void matches_same_password_with_different_hashes() {
			String encoded1 = passwordEncoder.encode(RAW_PASSWORD);
			String encoded2 = passwordEncoder.encode(RAW_PASSWORD);

			assertThat(passwordEncoder.matches(RAW_PASSWORD, encoded1)).isTrue();
			assertThat(passwordEncoder.matches(RAW_PASSWORD, encoded2)).isTrue();
		}
	}
}

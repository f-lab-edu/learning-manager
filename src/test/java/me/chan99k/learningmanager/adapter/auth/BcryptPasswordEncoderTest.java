package me.chan99k.learningmanager.adapter.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BcryptPasswordEncoderTest {

	private final BcryptPasswordEncoder encoder = new BcryptPasswordEncoder();

	@Test
	@DisplayName("[Success] 문자열을 인코딩하면 null 이 아닌 해시를 반환한다")
	void test01() {
		String raw = "mySecret123!";
		String encoded = encoder.encode(raw);

		assertNotNull(encoded);
		assertNotEquals(raw, encoded); // 평문 그대로 나오면 안 됨
	}

	@Test
	@DisplayName("[Success] matches는 인코딩된 값과 원문을 올바르게 비교한다")
	void matchesShouldReturnTrueForValidPassword() {
		String raw = "mySecret123!";
		String encoded = encoder.encode(raw);

		assertTrue(encoder.matches(raw, encoded));
		assertFalse(encoder.matches("wrongPassword", encoded));
	}
}
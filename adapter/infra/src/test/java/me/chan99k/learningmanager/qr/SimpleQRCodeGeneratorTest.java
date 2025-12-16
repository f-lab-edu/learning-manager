package me.chan99k.learningmanager.qr;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("SimpleQRCodeGenerator 테스트")
class SimpleQRCodeGeneratorTest {

	private static final Long SESSION_ID = 1L;
	private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:00:00Z");

	private Clock fixedClock;
	private SimpleQRCodeGenerator generator;

	@BeforeEach
	void setUp() {
		fixedClock = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
		generator = new SimpleQRCodeGenerator(fixedClock);
	}

	@Nested
	@DisplayName("generateQrCode")
	class GenerateQrCodeTest {

		@Test
		@DisplayName("[Success] 세션 ID와 만료시간으로 QR 코드를 생성한다")
		void test01() {
			Instant expiresAt = FIXED_INSTANT.plus(10, ChronoUnit.MINUTES);

			String qrCode = generator.generateQrCode(SESSION_ID, expiresAt);

			assertThat(qrCode).isEqualTo("SESSION_1_" + expiresAt.toEpochMilli());
		}

		@Test
		@DisplayName("[Success] 기본 만료시간(5분)으로 QR 코드를 생성한다")
		void test02() {
			Instant expectedExpiry = FIXED_INSTANT.plus(5, ChronoUnit.MINUTES);

			String qrCode = generator.generateQrCode(SESSION_ID);

			assertThat(qrCode).isEqualTo("SESSION_1_" + expectedExpiry.toEpochMilli());
		}
	}

	@Nested
	@DisplayName("validateQrCode")
	class ValidateQrCodeTest {

		@Test
		@DisplayName("[Success] 유효한 QR 코드면 true를 반환한다")
		void test01() {
			Instant expiresAt = FIXED_INSTANT.plus(10, ChronoUnit.MINUTES);
			String qrCode = generator.generateQrCode(SESSION_ID, expiresAt);

			boolean result = generator.validateQrCode(qrCode, SESSION_ID);

			assertThat(result).isTrue();
		}

		@ParameterizedTest(name = "[Failure] 잘못된 QR 코드: {0}")
		@NullSource
		@ValueSource(strings = {"INVALID_1_12345", "SESSION_INVALID"})
		@DisplayName("[Failure] null이거나 잘못된 형식이면 false를 반환한다")
		void invalidQrCodeReturnsFalse(String invalidQrCode) {
			boolean result = generator.validateQrCode(invalidQrCode, SESSION_ID);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Failure] 다른 세션 ID면 false를 반환한다")
		void test04() {
			Instant expiresAt = FIXED_INSTANT.plus(10, ChronoUnit.MINUTES);
			String qrCode = generator.generateQrCode(SESSION_ID, expiresAt);

			boolean result = generator.validateQrCode(qrCode, 999L);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Failure] 만료된 QR 코드면 false를 반환한다")
		void test05() {
			// 과거 시간으로 만료된 QR 코드 생성
			Instant expiredAt = FIXED_INSTANT.minus(1, ChronoUnit.MINUTES);
			String qrCode = "SESSION_1_" + expiredAt.toEpochMilli();

			boolean result = generator.validateQrCode(qrCode, SESSION_ID);

			assertThat(result).isFalse();
		}
	}
}

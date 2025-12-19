package me.chan99k.learningmanager.qr;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;

@DisplayName("JwtQRCodeGenerator 테스트")
class JwtQRCodeGeneratorTest {

	private static final Long SESSION_ID = 1L;
	private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:00:00Z");
	// 테스트용 256비트 시크릿 키 (Base64 인코딩)
	private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
		Jwts.SIG.HS256.key().build().getEncoded()
	);

	private Clock fixedClock;
	private JwtQRCodeGenerator generator;

	@BeforeEach
	void setUp() {
		fixedClock = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
		generator = new JwtQRCodeGenerator(fixedClock, TEST_SECRET);
	}

	@Nested
	@DisplayName("generateQrCode")
	class GenerateQrCodeTest {

		@Test
		@DisplayName("[Success] JWT 형식의 QR 코드를 생성한다")
		void test01() {
			Instant expiresAt = FIXED_INSTANT.plus(10, ChronoUnit.MINUTES);

			String qrCode = generator.generateQrCode(SESSION_ID, expiresAt);

			// JWT는 header.payload.signature 형식
			assertThat(qrCode.split("\\.")).hasSize(3);
		}

		@Test
		@DisplayName("[Success] 생성된 QR 코드는 유효성 검증을 통과한다")
		void test02() {
			Instant expiresAt = FIXED_INSTANT.plus(10, ChronoUnit.MINUTES);

			String qrCode = generator.generateQrCode(SESSION_ID, expiresAt);

			assertThat(generator.validateQrCode(qrCode, SESSION_ID)).isTrue();
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

		@Test
		@DisplayName("[Failure] 다른 세션 ID면 false를 반환한다")
		void test02() {
			Instant expiresAt = FIXED_INSTANT.plus(10, ChronoUnit.MINUTES);
			String qrCode = generator.generateQrCode(SESSION_ID, expiresAt);

			boolean result = generator.validateQrCode(qrCode, 999L);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Failure] 만료된 QR 코드면 false를 반환한다")
		void test03() {
			// 만료 시간이 현재보다 과거인 QR 코드 생성
			Instant expiredAt = FIXED_INSTANT.minus(1, ChronoUnit.MINUTES);
			String qrCode = generator.generateQrCode(SESSION_ID, expiredAt);

			boolean result = generator.validateQrCode(qrCode, SESSION_ID);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Failure] 잘못된 형식의 토큰이면 false를 반환한다")
		void test04() {
			boolean result = generator.validateQrCode("invalid.jwt.token", SESSION_ID);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Failure] 다른 시크릿으로 서명된 토큰이면 false를 반환한다")
		void test05() {
			// 다른 시크릿으로 생성된 generator
			String otherSecret = Base64.getEncoder().encodeToString(
				Jwts.SIG.HS256.key().build().getEncoded()
			);
			JwtQRCodeGenerator otherGenerator = new JwtQRCodeGenerator(fixedClock, otherSecret);

			Instant expiresAt = FIXED_INSTANT.plus(10, ChronoUnit.MINUTES);
			String qrCode = otherGenerator.generateQrCode(SESSION_ID, expiresAt);

			// 원래 generator로 검증 시 실패
			boolean result = generator.validateQrCode(qrCode, SESSION_ID);

			assertThat(result).isFalse();
		}
	}
}

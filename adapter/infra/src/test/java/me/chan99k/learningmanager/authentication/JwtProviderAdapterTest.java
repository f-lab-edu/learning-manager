package me.chan99k.learningmanager.authentication;

import static org.assertj.core.api.Assertions.*;

import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.chan99k.learningmanager.exception.DomainException;

class JwtProviderAdapterTest {

	private static final Long MEMBER_ID = 1L;
	private static final String EMAIL = "test@example.com";
	private static final List<String> ROLES = List.of("MEMBER");
	private static final long EXPIRATION_SECONDS = 3600L;
	private static final String ISSUER = "test-issuer";

	private static final String SECRET = Base64.getEncoder().encodeToString(
		"test-secret-key-for-jwt-signing-must-be-256-bits-long".getBytes()
	);

	private static final String DIFFERENT_SECRET = Base64.getEncoder().encodeToString(
		"different-secret-key-for-jwt-must-be-256-bits-long!!".getBytes()
	);

	JwtProviderAdapter jwtProvider;

	@BeforeEach
	void setUp() {
		jwtProvider = new JwtProviderAdapter(SECRET, EXPIRATION_SECONDS, ISSUER);
	}

	@Nested
	@DisplayName("createAccessToken 메서드")
	class CreateAccessTokenTest {

		@Test
		@DisplayName("유효한 JWT 문자열을 생성한다")
		void creates_valid_jwt_string() {
			String token = jwtProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			assertThat(token).isNotNull();
			assertThat(token.split("\\.")).hasSize(3);
		}

		@Test
		@DisplayName("생성된 토큰에 memberId 클레임을 포함한다")
		void token_contains_member_id_claim() {
			String token = jwtProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			JwtProvider.Claims claims = jwtProvider.validateAndGetClaims(token);
			assertThat(claims.memberId()).isEqualTo(MEMBER_ID);
		}

		@Test
		@DisplayName("생성된 토큰에 email 클레임을 포함한다")
		void token_contains_email_claim() {
			String token = jwtProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			JwtProvider.Claims claims = jwtProvider.validateAndGetClaims(token);
			assertThat(claims.email()).isEqualTo(EMAIL);
		}

		@Test
		@DisplayName("생성된 토큰에 roles 클레임을 포함한다")
		void token_contains_roles_claim() {
			String token = jwtProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			JwtProvider.Claims claims = jwtProvider.validateAndGetClaims(token);
			assertThat(claims.roles()).containsExactlyElementsOf(ROLES);
		}
	}

	@Nested
	@DisplayName("validateAndGetClaims 메서드")
	class ValidateAndGetClaimsTest {

		@Test
		@DisplayName("유효한 토큰에서 클레임을 추출한다")
		void extracts_claims_from_valid_token() {
			String token = jwtProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			JwtProvider.Claims claims = jwtProvider.validateAndGetClaims(token);

			assertThat(claims.memberId()).isEqualTo(MEMBER_ID);
			assertThat(claims.email()).isEqualTo(EMAIL);
			assertThat(claims.roles()).isEqualTo(ROLES);
			assertThat(claims.expiresAt()).isNotNull();
		}

		@Test
		@DisplayName("만료된 토큰은 EXPIRED_TOKEN 예외를 던진다")
		void throws_expired_token_exception() {
			JwtProviderAdapter shortLivedProvider = new JwtProviderAdapter(SECRET, 0L, ISSUER);
			String expiredToken = shortLivedProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			assertThatThrownBy(() -> jwtProvider.validateAndGetClaims(expiredToken))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.EXPIRED_TOKEN);
		}

		@Test
		@DisplayName("잘못된 형식의 토큰은 INVALID_TOKEN 예외를 던진다")
		void throws_invalid_token_for_malformed_token() {
			String malformedToken = "invalid.token.format";

			assertThatThrownBy(() -> jwtProvider.validateAndGetClaims(malformedToken))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.INVALID_TOKEN);
		}

		@Test
		@DisplayName("서명이 다른 토큰은 INVALID_TOKEN 예외를 던진다")
		void throws_invalid_token_for_wrong_signature() {
			JwtProviderAdapter differentProvider = new JwtProviderAdapter(
				DIFFERENT_SECRET, EXPIRATION_SECONDS, ISSUER
			);
			String tokenWithDifferentSignature = differentProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			assertThatThrownBy(() -> jwtProvider.validateAndGetClaims(tokenWithDifferentSignature))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.INVALID_TOKEN);
		}
	}

	@Nested
	@DisplayName("isValid 메서드")
	class IsValidTest {

		@Test
		@DisplayName("유효한 토큰은 true를 반환한다")
		void returns_true_for_valid_token() {
			String token = jwtProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			boolean result = jwtProvider.isValid(token);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("만료된 토큰은 false를 반환한다")
		void returns_false_for_expired_token() {
			JwtProviderAdapter shortLivedProvider = new JwtProviderAdapter(SECRET, 0L, ISSUER);
			String expiredToken = shortLivedProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			boolean result = jwtProvider.isValid(expiredToken);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("잘못된 형식의 토큰은 false를 반환한다")
		void returns_false_for_malformed_token() {
			boolean result = jwtProvider.isValid("invalid.token");

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("서명이 다른 토큰은 false를 반환한다")
		void returns_false_for_wrong_signature() {
			JwtProviderAdapter differentProvider = new JwtProviderAdapter(
				DIFFERENT_SECRET, EXPIRATION_SECONDS, ISSUER
			);
			String tokenWithDifferentSignature = differentProvider.createAccessToken(MEMBER_ID, EMAIL, ROLES);

			boolean result = jwtProvider.isValid(tokenWithDifferentSignature);

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("getAccessTokenExpirationSeconds 메서드")
	class GetAccessTokenExpirationSecondsTest {

		@Test
		@DisplayName("설정된 만료 시간을 반환한다")
		void returns_configured_expiration_seconds() {
			long result = jwtProvider.getAccessTokenExpirationSeconds();

			assertThat(result).isEqualTo(EXPIRATION_SECONDS);
		}
	}
}

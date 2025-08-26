package me.chan99k.learningmanager.adapter.auth;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.chan99k.learningmanager.common.exception.AuthenticateException;

class JwtTokenProviderTest {

	private final String testIssuer = "test-issuer";
	private final String testAudience = "test-audience";
	private final String testSecret = "test-secret-key-for-jwt-token-generation-and-validation";
	private final long testValidityInSeconds = 3600;
	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(
			testIssuer,
			testAudience,
			testSecret,
			testValidityInSeconds
		);
	}

	@Test
	@DisplayName("유효한 회원 ID로 JWT 토큰을 생성할 수 있다")
	void createToken_WithValidMemberId_ReturnsValidToken() {
		// given
		Long memberId = 12345L;

		// when
		String token = jwtTokenProvider.createToken(memberId);

		// then
		assertThat(token).isNotNull();
		assertThat(token).isNotEmpty();

		// JWT 구조 확인 (header.payload.signature)
		String[] parts = token.split("\\.");
		assertThat(parts).hasSize(3);
	}

	@Test
	@DisplayName("생성된 토큰에서 올바른 클레임 정보를 확인할 수 있다")
	void createToken_VerifyTokenClaims() {
		// given
		Long memberId = 12345L;

		// when
		String token = jwtTokenProvider.createToken(memberId);

		// then
		Claims claims = Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.build()
			.parseSignedClaims(token)
			.getPayload();

		assertThat(claims.getSubject()).isEqualTo(String.valueOf(memberId));
		assertThat(claims.getIssuer()).isEqualTo(testIssuer);
		assertThat(claims.getAudience()).contains(testAudience);
		assertThat(claims.getIssuedAt()).isNotNull();
		assertThat(claims.getExpiration()).isNotNull();
	}

	@Test
	@DisplayName("유효한 토큰을 검증하면 true를 반환한다")
	void validateToken_WithValidToken_ReturnsTrue() {
		// given
		Long memberId = 12345L;
		String token = jwtTokenProvider.createToken(memberId);

		// when & then
		assertThat(jwtTokenProvider.validateToken(token)).isTrue();
	}

	@Test
	@DisplayName("잘못된 시크릿으로 생성된 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithInvalidSecret_ThrowsException() {
		// given
		String invalidToken = Jwts.builder()
			.subject("12345")
			.issuer(testIssuer)
			.audience().add(testAudience).and()
			.signWith(Keys.hmacShaKeyFor("wrong-secret-key-for-jwt-token-generation-validation".getBytes()))
			.compact();

		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("잘못된 발급자를 가진 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithInvalidIssuer_ThrowsException() {
		// given
		String tokenWithWrongIssuer = Jwts.builder()
			.subject("12345")
			.issuer("wrong-issuer")
			.audience().add(testAudience).and()
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenWithWrongIssuer))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("잘못된 대상자를 가진 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithInvalidAudience_ThrowsException() {
		// given
		String tokenWithWrongAudience = Jwts.builder()
			.subject("12345")
			.issuer(testIssuer)
			.audience().add("wrong-audience").and()
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenWithWrongAudience))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("만료된 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithExpiredToken_ThrowsException() {
		// given - 과거 시간으로 만료된 토큰 직접 생성
		Instant pastTime = Instant.now().minusSeconds(3600); // 1시간 전
		String expiredToken = Jwts.builder()
			.subject("12345")
			.issuer(testIssuer)
			.audience().add(testAudience).and()
			.issuedAt(Date.from(pastTime.minusSeconds(7200))) // 2시간 전 발급
			.expiration(Date.from(pastTime)) // 1시간 전 만료
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("유효한 토큰에서 회원 ID를 추출할 수 있다")
	void getMemberIdFromToken_WithValidToken_ReturnsMemberId() {
		// given
		Long memberId = 12345L;
		String token = jwtTokenProvider.createToken(memberId);

		// when
		String extractedMemberId = jwtTokenProvider.getMemberIdFromToken(token);

		// then
		assertThat(extractedMemberId).isEqualTo(String.valueOf(memberId));
	}

	@Test
	@DisplayName("subject가 없는 토큰에서 회원 ID를 추출하면 예외가 발생한다")
	void getMemberIdFromToken_WithoutSubject_ThrowsException() {
		// given - subject 클레임 없이 토큰 생성
		String tokenWithoutSubject = Jwts.builder()
			.issuer(testIssuer)
			.audience().add(testAudience).and()
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromToken(tokenWithoutSubject))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰의 subject 가 유효하지 않습니다");
	}

	@Test
	@DisplayName("잘못된 토큰에서 회원 ID를 추출하면 예외가 발생한다")
	void getMemberIdFromToken_WithInvalidToken_ThrowsException() {
		// given
		String invalidToken = "invalid.jwt.token";

		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromToken(invalidToken))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("인증에 실패하였습니다");
	}

	@Test
	@DisplayName("null 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithNullToken_ThrowsException() {
		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.validateToken(null))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("빈 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithEmptyToken_ThrowsException() {
		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.validateToken(""))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}
}
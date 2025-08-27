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
	private static final String testSubject = "12345";

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
	@DisplayName("[Success] 유효한 회원 ID로 JWT 토큰을 생성할 수 있다")
	void createToken_WithValidMemberId_ReturnsValidToken() {
		Long memberId = Long.valueOf(testSubject);

		String token = jwtTokenProvider.createToken(memberId);

		assertThat(token).isNotNull();
		assertThat(token).isNotEmpty();

		// JWT 구조 확인 (header.payload.signature)
		String[] parts = token.split("\\.");
		assertThat(parts).hasSize(3);
	}

	@Test
	@DisplayName("[Success] 생성된 토큰에서 올바른 클레임 정보를 확인할 수 있다")
	void createToken_VerifyTokenClaims() {
		// given
		Long memberId = Long.valueOf(testSubject);

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
	@DisplayName("[Success] 유효한 토큰을 검증하면 true를 반환한다")
	void validateToken_WithValidToken_ReturnsTrue() {
		Long memberId = Long.valueOf(testSubject);
		String token = jwtTokenProvider.createToken(memberId);

		assertThat(jwtTokenProvider.validateToken(token)).isTrue();
	}

	@Test
	@DisplayName("[Failure] 잘못된 시크릿으로 생성된 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithInvalidSecret_ThrowsException() {
		String invalidToken = Jwts.builder()
			.subject(testSubject)
			.issuer(testIssuer)
			.audience().add(testAudience).and()
			.signWith(Keys.hmacShaKeyFor("wrong-secret-key-for-jwt-token-generation-validation".getBytes())) // 잘못된 시크릿
			.compact();

		assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("[Failure] 다른 발급자(issuer) 정보를 가진 토큰으로 인증하면 실패한다")
	void validateToken_WithDifferentIssuer_FailsAuthentication() {
		// given - 올바른 audience, subject, secret이지만 다른 issuer
		String tokenWithDifferentIssuer = Jwts.builder()
			.subject(testSubject)
			.issuer("different-issuer")
			.audience().add(testAudience).and()
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + testValidityInSeconds * 1000))
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenWithDifferentIssuer))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("[Failure] 다른 audience 정보를 가진 토큰으로 인증하면 실패한다")
	void validateToken_WithDifferentAudience_FailsAuthentication() {
		String tokenWithDifferentAudience = Jwts.builder()
			.subject(testSubject)
			.issuer(testIssuer)
			.audience().add("different-audience").and() // 다른 aud
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + testValidityInSeconds * 1000))
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		// when & then
		assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenWithDifferentAudience))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("[Failure] 만료된 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithExpiredToken_ThrowsException() {
		Instant pastTime = Instant.now().minusSeconds(3600); // 1시간 전
		String expiredToken = Jwts.builder()
			.subject(testSubject)
			.issuer(testIssuer)
			.audience().add(testAudience).and()
			.issuedAt(Date.from(pastTime.minusSeconds(7200))) // 2시간 전 발급
			.expiration(Date.from(pastTime)) // 1시간 전 만료
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("[Success] 유효한 토큰에서 회원 ID를 추출할 수 있다")
	void getMemberIdFromToken_WithValidToken_ReturnsMemberId() {
		Long memberId = Long.valueOf(testSubject);
		String token = jwtTokenProvider.createToken(memberId);

		String extractedMemberId = jwtTokenProvider.getMemberIdFromToken(token);

		assertThat(extractedMemberId).isEqualTo(String.valueOf(memberId));
	}

	@Test
	@DisplayName("[Failure] subject가 없는 토큰에서 회원 ID를 추출하면 예외가 발생한다")
	void getMemberIdFromToken_WithoutSubject_ThrowsException() {
		// given - subject 클레임 없이 토큰 생성
		String tokenWithoutSubject = Jwts.builder()
			.issuer(testIssuer)
			.audience().add(testAudience).and()
			.signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.compact();

		assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromToken(tokenWithoutSubject))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰의 subject 가 유효하지 않습니다");
	}

	@Test
	@DisplayName("[Failure] 잘못된 토큰에서 회원 ID를 추출하면 예외가 발생한다")
	void getMemberIdFromToken_WithInvalidToken_ThrowsException() {
		String invalidToken = "invalid.jwt.token";

		assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromToken(invalidToken))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("인증에 실패하였습니다");
	}

	@Test
	@DisplayName("[Failure] null 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithNullToken_ThrowsException() {

		assertThatThrownBy(() -> jwtTokenProvider.validateToken(null))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}

	@Test
	@DisplayName("[Failure] 빈 토큰을 검증하면 예외가 발생한다")
	void validateToken_WithEmptyToken_ThrowsException() {

		assertThatThrownBy(() -> jwtTokenProvider.validateToken(""))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");
	}
}
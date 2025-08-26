package me.chan99k.learningmanager.adapter.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.chan99k.learningmanager.common.exception.AuthenticateException;

@Component
public class JwtTokenProvider {
	private final String tokenIssuer;
	private final String tokenAudience;
	private final SecretKey secretKey;
	private final long tokenValidityInMilliseconds;

	public JwtTokenProvider(
		@Value("${jwt.access-token.issuer}")
		String tokenIssuer,
		@Value(("${jwt.access-token.audience}"))
		String tokenAudience,
		@Value("${jwt.secret}")
		String secretKey,
		@Value("${jwt.access-token.validity-in-seconds}")
		long tokenValidityInSeconds
	) {
		this.tokenIssuer = tokenIssuer;
		this.tokenAudience = tokenAudience;
		this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
		this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
	}

	public String createToken(Long memberId) {
		Instant now = Instant.now();
		Instant expires = now.plus(tokenValidityInMilliseconds, ChronoUnit.MILLIS);

		return Jwts.builder()
			.subject(String.valueOf(memberId))
			.issuer(tokenIssuer)
			.audience().add(tokenAudience).and()
			.issuedAt(Date.from(now))
			.expiration(Date.from(expires))
			.signWith(secretKey)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.requireIssuer(tokenIssuer)     // 발급자 검증
				.requireAudience(tokenAudience)
				.clockSkewSeconds(60)           // 시간 오차 허용 (1분)
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			throw new AuthenticateException(AuthProblemCode.FAILED_TO_VALIDATE_TOKEN, e);
		}
	}

	public String getMemberIdFromToken(String token) {
		try {
			Claims payload = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			String subject = payload.getSubject();
			if (subject == null) {
				throw new AuthenticateException(AuthProblemCode.INVALID_TOKEN_SUBJECT);
			}

			return payload.getSubject();
		} catch (Exception e) {
			throw new AuthenticateException(AuthProblemCode.FAILED_TO_AUTHENTICATE, e);
		}
	}
}

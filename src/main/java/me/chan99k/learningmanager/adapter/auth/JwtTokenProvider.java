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

@Component
public class JwtTokenProvider {
	private final SecretKey secretKey;
	private final long tokenValidityInMilliseconds;

	public JwtTokenProvider(
		@Value("${jwt.secret}")
		String secretKey,
		@Value("${jwt.access-token.validity-in-seconds}")
		long tokenValidityInSeconds
	) {
		this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
		this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
	}

	public String createToken(Long memberId) {
		Instant now = Instant.now();
		Instant expires = now.plus(tokenValidityInMilliseconds, ChronoUnit.MILLIS);

		return Jwts.builder()
			.subject(String.valueOf(memberId))
			.issuedAt(Date.from(now))
			.expiration(Date.from(expires))
			.signWith(secretKey)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getMemberIdFromToken(String token) {
		Claims payload = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		return payload.getSubject();
	}
}

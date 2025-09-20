package me.chan99k.learningmanager.adapter.auth.crypto.jwt;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.chan99k.learningmanager.adapter.auth.core.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.token.TokenFormat;
import me.chan99k.learningmanager.adapter.auth.token.TokenType;
import me.chan99k.learningmanager.common.exception.AuthenticationException;

@Component
public class JwtDecoder {

	private final byte[] secretKey;

	public JwtDecoder(@Value("${jwt.secret}") String secret) {
		if (secret == null || secret.trim().isEmpty()) {
			throw new IllegalArgumentException("[System] JWT secret 은 비어있거나 null 일 수 없습니다");
		}
		if (secret.length() < 32) {
			throw new IllegalArgumentException("[System] JWT secret 은 32자 이상이어야 합니다");
		}
		this.secretKey = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
	}

	public JwtTokenData decode(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(secretKey))
				.build()
				.parseSignedClaims(token)
				.getPayload();

			List<?> rawRoles = claims.get("roles", List.class);
			List<String> roles = Collections.emptyList();
			if (!rawRoles.isEmpty()) {
				roles = rawRoles.stream()
					.filter(String.class::isInstance)
					.map(String.class::cast)
					.toList();
			}

			return JwtTokenData.builder()
				.type(TokenType.ACCESS)
				.format(TokenFormat.JWT)
				.payload(Long.parseLong(claims.getSubject()))
				.roles(roles)
				.issuedAt(claims.getIssuedAt().toInstant())
				.expiresAt(claims.getExpiration().toInstant())
				.issuer(claims.getIssuer())
				.subject(claims.getSubject())
				.build();

		} catch (Exception e) {
			throw new AuthenticationException(AuthProblemCode.FAILED_TO_PARSE_JWT, e);
		}
	}

	public String encode(String subject, Instant expiration) {
		return Jwts.builder()
			.subject(subject)
			.issuedAt(Date.from(Instant.now()))
			.expiration(Date.from(expiration)).issuer("learning-manager")
			.signWith(Keys.hmacShaKeyFor(secretKey))
			.compact();
	}

	public boolean validate(String token) {
		try {
			Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(secretKey))
				.build()
				.parseSignedClaims(token)
				.getPayload();

			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
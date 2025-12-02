package me.chan99k.learningmanager.auth;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import me.chan99k.learningmanager.exception.DomainException;

@Component
public class JwtProviderAdapter implements JwtProvider {
	private final SecretKey secretKey;
	private final long accessTokenExpirationSeconds;
	private final String issuer;

	public JwtProviderAdapter(
		@Value("${auth.jwt.secret}") String secret,
		@Value("${auth.jwt.access-token-expiration-seconds:3600}") long accessTokenExpirationSeconds,
		@Value("${auth.jwt.issuer:learning-manager}") String issuer
	) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
		this.issuer = issuer;
	}

	@Override
	public String createAccessToken(Long memberId, String email, List<String> roles) {
		Instant now = Instant.now();
		Instant expiration = now.plusSeconds(accessTokenExpirationSeconds);

		return Jwts.builder()
			.issuer(issuer)
			.subject(String.valueOf(memberId))
			.audience().add("learning-manager-api").and()
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiration))
			.id(UUID.randomUUID().toString())
			.claim("member_id", memberId)
			.claim("email", email)
			.claim("roles", roles)
			.signWith(secretKey)
			.compact();
	}

	@Override
	public Claims validateAndGetClaims(String token) {
		try {
			var claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			Long memberId = claims.get("member_id", Long.class);
			String email = claims.get("email", String.class);
			@SuppressWarnings("unchecked")
			List<String> roles = claims.get("roles", List.class);
			Instant expiresAt = claims.getExpiration().toInstant();

			return new Claims(memberId, email, roles, expiresAt);

		} catch (ExpiredJwtException e) {
			throw new DomainException(AuthProblemCode.EXPIRED_TOKEN);
		} catch (JwtException e) {
			throw new DomainException(AuthProblemCode.INVALID_TOKEN);
		}
	}

	@Override
	public boolean isValid(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	@Override
	public long getAccessTokenExpirationSeconds() {
		return accessTokenExpirationSeconds;
	}
}

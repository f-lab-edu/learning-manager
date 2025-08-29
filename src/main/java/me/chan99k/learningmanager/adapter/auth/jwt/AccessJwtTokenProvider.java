package me.chan99k.learningmanager.adapter.auth.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.chan99k.learningmanager.adapter.auth.AccessTokenProvider;
import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.TokenRevocationProvider;
import me.chan99k.learningmanager.common.exception.AuthenticateException;

@Component
public class AccessJwtTokenProvider extends AbstractJwtTokenProvider<SecretKey>
	implements AccessTokenProvider<Long> {

	private static final String TOKEN_PURPOSE = "ACCESS";

	private final String issuer;
	private final String audience;
	private final SecretKey secretKey;
	private final long validityInMilliseconds;

	public AccessJwtTokenProvider(
		TokenRevocationProvider<SecretKey> revocationProvider,
		@Value("${jwt.access-token.issuer}") String issuer,
		@Value("${jwt.access-token.audience}") String audience,
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.access-token.validity-in-seconds}") long validityInSeconds
	) {
		super(revocationProvider);
		this.issuer = issuer;
		this.audience = audience;
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
		this.validityInMilliseconds = validityInSeconds * 1000;
	}

	@Override
	public String createAccessToken(Long memberId) {
		Instant now = Instant.now();
		Instant expires = now.plus(validityInMilliseconds, ChronoUnit.MILLIS);

		return Jwts.builder()
			.issuer(issuer)
			.subject(String.valueOf(memberId))
			.audience().add(audience).and()
			.issuedAt(Date.from(now))
			.expiration(Date.from(expires))
			.claim("purpose", TOKEN_PURPOSE)
			.signWith(secretKey)
			.compact();
	}

	@Override
	public boolean validateAccessToken(String accessToken) {
		try {
			if (isRevoked(accessToken)) {
				return false;
			}
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.requireIssuer(issuer)
				.requireAudience(audience)
				.clockSkewSeconds(60)
				.build()
				.parseSignedClaims(accessToken)
				.getPayload();

			return TOKEN_PURPOSE.equals(claims.get("purpose", String.class));
		} catch (Exception e) {
			throw new AuthenticateException(AuthProblemCode.FAILED_TO_VALIDATE_TOKEN, e);
		}
	}

	@Override
	public Long getIdFromAccessToken(String accessToken) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(accessToken)
				.getPayload();

			return Long.valueOf(claims.getSubject());
		} catch (Exception e) {
			throw new AuthenticateException(AuthProblemCode.FAILED_TO_AUTHENTICATE, e);
		}
	}

	@Override
	public String getIdAsString(String accessToken) {
		return String.valueOf(getIdFromAccessToken(accessToken));
	}

	@Override
	protected SecretKey getValidator() {
		return this.secretKey;
	}
}

package me.chan99k.learningmanager.qr;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import me.chan99k.learningmanager.attendance.QRCodeGenerator;

@Primary // TODO::  @ConditionalOnProperty 또는 @Profile을 사용하여 환경별로 명시적으로 선택하도록 하기
@Component
public class JwtQRCodeGenerator implements QRCodeGenerator {

	private static final String TOKEN_TYPE = "ATTENDANCE";

	private final Clock clock;
	private final SecretKey secretKey;

	public JwtQRCodeGenerator(
		Clock clock,
		@Value("${auth.jwt.secret}") String secret
	) {
		this.clock = clock;
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}

	@Override
	public String generateQrCode(Long sessionId, Instant expiresAt) {
		return Jwts.builder()
			.claim("sessionId", sessionId)
			.claim("type", TOKEN_TYPE)
			.issuedAt(Date.from(clock.instant()))
			.expiration(Date.from(expiresAt))
			.signWith(secretKey)
			.compact();
	}

	@Override
	public boolean validateQrCode(String qrCode, Long sessionId) {
		try {
			var claims = Jwts.parser()
				.verifyWith(secretKey)
				.clock(() -> Date.from(clock.instant()))
				.build()
				.parseSignedClaims(qrCode)
				.getPayload();

			Long tokenSessionId = claims.get("sessionId", Long.class);
			String type = claims.get("type", String.class);

			return sessionId.equals(tokenSessionId) && TOKEN_TYPE.equals(type);
		} catch (JwtException e) {
			return false;
		}
	}
}

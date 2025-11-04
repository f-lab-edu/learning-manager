package me.chan99k.learningmanager.adapter.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.application.member.requires.RefreshTokenProvider;

@Component
public class RefreshTokenAdapter implements RefreshTokenProvider {
	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final AccessTokenAdapter accessTokenProvider;

	// 간단한 메모리 기반 토큰 저장소 (실제 운영에서는 Redis 등 사용)
	private final ConcurrentHashMap<String, Boolean> validTokens = new ConcurrentHashMap<>();

	public RefreshTokenAdapter(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, AccessTokenAdapter accessTokenProvider) {
		this.jwtEncoder = jwtEncoder;
		this.jwtDecoder = jwtDecoder;
		this.accessTokenProvider = accessTokenProvider;
	}

	@Override
	public String generateRefreshToken(Long memberId, String email) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("learning-manager")
			.issuedAt(now)
			.expiresAt(now.plus(Duration.ofDays(7))) // 7일 유효
			.subject(memberId.toString())
			.claim("email", email)
			.claim("type", "refresh")
			.build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
		validTokens.put(token, true);
		return token;
	}

	@Override
	public RefreshResult refreshAccessToken(String refreshToken) {
		if (!validateRefreshToken(refreshToken)) {
			throw new IllegalArgumentException("Invalid refresh token");
		}

		try {
			Jwt jwt = jwtDecoder.decode(refreshToken);
			String email = jwt.getClaimAsString("email");
			Long memberId = Long.valueOf(jwt.getSubject());

			// 기존 리프레시 토큰 무효화
			validTokens.remove(refreshToken);

			// 새로운 토큰들 생성
			String newAccessToken = accessTokenProvider.generateAccessToken(memberId, email);
			String newRefreshToken = generateRefreshToken(memberId, email);

			return new RefreshResult(newAccessToken, newRefreshToken);
		} catch (JwtException e) {
			throw new IllegalArgumentException("Invalid refresh token", e);
		}
	}

	@Override
	public void revokeRefreshToken(String refreshToken) {
		validTokens.remove(refreshToken);
	}

	@Override
	public boolean validateRefreshToken(String refreshToken) {
		if (!validTokens.containsKey(refreshToken)) {
			return false;
		}

		try {
			Jwt jwt = jwtDecoder.decode(refreshToken);
			String tokenType = jwt.getClaimAsString("type");
			return "refresh".equals(tokenType) && jwt.getExpiresAt().isAfter(Instant.now());
		} catch (JwtException e) {
			validTokens.remove(refreshToken); // 유효하지 않은 토큰 제거
			return false;
		}
	}
}
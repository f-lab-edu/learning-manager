package me.chan99k.learningmanager.auth;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.application.member.AccessTokenProvider;

@Component
public class AccessTokenAdapter implements AccessTokenProvider {
	private final JwtEncoder jwtEncoder;

	public AccessTokenAdapter(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	@Override
	public String generateAccessToken(Long memberId, String email) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("learning-manager")
			.issuedAt(now)
			.expiresAt(now.plus(Duration.ofMinutes(30)))
			.subject(memberId.toString())
			.claim("email", email)
			.claim("type", "access")
			.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}
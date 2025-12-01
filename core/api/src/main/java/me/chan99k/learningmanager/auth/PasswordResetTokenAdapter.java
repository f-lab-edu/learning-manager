package me.chan99k.learningmanager.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.application.member.PasswordResetTokenProvider;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@Component
public class PasswordResetTokenAdapter implements PasswordResetTokenProvider {
	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet(); // TODO :: 개발용 인메모리임에 주의

	public PasswordResetTokenAdapter(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
		this.jwtEncoder = jwtEncoder;
		this.jwtDecoder = jwtDecoder;
	}

	@Override
	public String createResetToken(String email) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("learning-manager")
			.issuedAt(now)
			.expiresAt(now.plus(Duration.ofMinutes(30)))
			.subject(email)
			.claim("type", "password_reset")
			.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	@Override
	public boolean validateResetToken(String token) {
		if (invalidatedTokens.contains(token)) {
			return false;
		}

		try {
			Jwt jwt = jwtDecoder.decode(token);
			String type = jwt.getClaimAsString("type");
			return "password_reset".equals(type)
				&& jwt.getExpiresAt() != null
				&& jwt.getExpiresAt().isAfter(Instant.now());
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Email getEmailFromResetToken(String token) {
		if (!validateResetToken(token)) {
			throw new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		}

		try {
			Jwt jwt = jwtDecoder.decode(token);
			return Email.of(jwt.getSubject());
		} catch (Exception e) {
			throw new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN);
		}
	}

	@Override
	public void invalidateAfterUse(String token) {
		invalidatedTokens.add(token);
	}
}
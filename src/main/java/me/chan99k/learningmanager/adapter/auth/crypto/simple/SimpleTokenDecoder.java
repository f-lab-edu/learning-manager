package me.chan99k.learningmanager.adapter.auth.crypto.simple;

import java.time.Instant;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.common.exception.AuthenticationException;

@Component
public class SimpleTokenDecoder {

	public boolean supports(String token) {
		if (token == null || token.trim().isEmpty()) {
			return false;
		}

		try {
			String[] parts = token.split(":");
			return parts.length >= 2 && parts.length <= 4;
		} catch (Exception e) {
			return false;
		}
	}

	public SimpleTokenData decode(String token) {
		if (!supports(token)) {
			throw new AuthenticationException(AuthProblemCode.INVALID_TOKEN);
		}

		try {
			String[] parts = token.split(":");

			// Format: "subject:memberId" or "subject:memberId:value" or "subject:memberId:value:expiration"
			String subject = parts[0];
			Long memberId = Long.parseLong(parts[1]);
			String value = parts.length > 2 && !"NULL".equals(parts[2]) ? parts[2] : null;
			Instant expiresAt = parts.length > 3 ? Instant.ofEpochSecond(Long.parseLong(parts[3])) : null;

			return new SimpleTokenData(subject, memberId, value, expiresAt);

		} catch (NumberFormatException e) {
			throw new AuthenticationException(AuthProblemCode.INVALID_TOKEN, e);
		} catch (Exception e) {
			throw new AuthenticationException(AuthProblemCode.FAILED_TO_PARSE_TOKEN, e);
		}
	}

	public String encode(String subject, Long memberId, String value, Instant expiresAt) {
		StringBuilder token = new StringBuilder();
		token.append(subject).append(":").append(memberId);

		if (value != null) {
			token.append(":").append(value);
		}

		if (expiresAt != null) {
			if (value == null) {
				token.append("NULL:");  // value가 null이면 명시적 NULL 플레이스홀더 사용
			}
			token.append(":").append(expiresAt.getEpochSecond());
		}

		return token.toString();
	}

	public boolean validate(String token) {
		try {
			SimpleTokenData tokenData = decode(token);
			return !tokenData.isExpired();
		} catch (Exception e) {
			return false;
		}
	}
}
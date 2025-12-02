package me.chan99k.learningmanager.member;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class Credential {
	private final CredentialType type;
	private final String secret;
	private LocalDateTime lastUsedAt;

	public Credential(CredentialType type, String secret) {
		this.type = type;
		this.secret = secret;
	}

	public static Credential ofPassword(String hashedPassword) {
		return new Credential(CredentialType.PASSWORD, hashedPassword);
	}

	public static Credential ofOAuth(CredentialType oauthType, String providerId) {
		if (oauthType.equals(CredentialType.PASSWORD)) {
			throw new IllegalArgumentException("password 인증 플로우를 사용하세요");
		}

		return new Credential(oauthType, providerId);
	}

	public static Credential reconstitute(CredentialType type, String secret, Instant lastUsedAt) {
		Credential credential = new Credential(type, secret);
		credential.lastUsedAt = LocalDateTime.ofInstant(lastUsedAt, ZoneId.of("Asia/Seoul"));

		return credential;
	}

	public boolean isPasswordType() {
		return this.type == CredentialType.PASSWORD;
	}

	public void recordUsage() {
		this.lastUsedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
	}

	public CredentialType getType() {
		return type;
	}

	public String getSecret() {
		return secret;
	}

	public LocalDateTime getLastUsedAt() {
		return lastUsedAt;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		Credential that = (Credential)object;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}
}

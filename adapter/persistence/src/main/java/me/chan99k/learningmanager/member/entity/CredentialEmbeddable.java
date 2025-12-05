package me.chan99k.learningmanager.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import me.chan99k.learningmanager.member.CredentialType;

@Embeddable
public class CredentialEmbeddable {

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private CredentialType type;

	@Column(name = "secret", nullable = false)
	private String secret;

	@Column(name = "last_used_at")
	private LocalDateTime lastUsedAt;

	protected CredentialEmbeddable() {
	}

	private CredentialEmbeddable(Builder builder) {
		this.type = builder.type;
		this.secret = builder.secret;
		this.lastUsedAt = builder.lastUsedAt;
	}

	public static Builder builder() {
		return new Builder();
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


	public static class Builder {

		private CredentialType type;
		private String secret;
		private LocalDateTime lastUsedAt;

		public Builder type(CredentialType type) {
			this.type = type;
			return this;
		}

		public Builder secret(String secret) {
			this.secret = secret;
			return this;
		}

		public Builder lastUsedAt(LocalDateTime lastUsedAt) {
			this.lastUsedAt = lastUsedAt;
			return this;
		}

		public CredentialEmbeddable build() {
			return new CredentialEmbeddable(this);
		}
	}
}

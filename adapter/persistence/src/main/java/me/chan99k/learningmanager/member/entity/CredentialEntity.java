package me.chan99k.learningmanager.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.chan99k.learningmanager.member.CredentialType;

@Entity
@Table(name = "credential")
public class CredentialEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private CredentialType type;

	@Column(name = "secret", nullable = false)
	private String secret;

	@Column(name = "last_used_at")
	private LocalDateTime lastUsedAt;

	protected CredentialEntity() {
	}

	private CredentialEntity(Builder builder) {
		this.account = builder.account;
		this.type = builder.type;
		this.secret = builder.secret;
		this.lastUsedAt = builder.lastUsedAt;
	}

	public static Builder builder() {
		return new Builder();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public CredentialType getType() {
		return type;
	}

	public void setType(CredentialType type) {
		this.type = type;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public LocalDateTime getLastUsedAt() {
		return lastUsedAt;
	}

	public void setLastUsedAt(LocalDateTime lastUsedAt) {
		this.lastUsedAt = lastUsedAt;
	}

	public static class Builder {
		private AccountEntity account;
		private CredentialType type;
		private String secret;
		private LocalDateTime lastUsedAt;

		public Builder account(AccountEntity account) {
			this.account = account;
			return this;
		}

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

		public CredentialEntity build() {
			return new CredentialEntity(this);
		}
	}
}

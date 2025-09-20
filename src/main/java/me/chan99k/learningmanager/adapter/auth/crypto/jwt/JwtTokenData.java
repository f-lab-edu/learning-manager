package me.chan99k.learningmanager.adapter.auth.crypto.jwt;

import java.time.Instant;
import java.util.List;

import me.chan99k.learningmanager.adapter.auth.token.TokenData;
import me.chan99k.learningmanager.adapter.auth.token.TokenFormat;
import me.chan99k.learningmanager.adapter.auth.token.TokenType;

public class JwtTokenData implements TokenData {

	private final TokenType type;
	private final TokenFormat format;
	private final Object payload;
	private final Instant issuedAt;
	private final Instant expiresAt;
	private final String issuer;
	private final String subject;
	private final List<String> roles;

	private JwtTokenData(Builder builder) {
		this.type = builder.type;
		this.format = builder.format;
		this.payload = builder.payload;
		this.issuedAt = builder.issuedAt;
		this.expiresAt = builder.expiresAt;
		this.issuer = builder.issuer;
		this.subject = builder.subject;
		this.roles = builder.roles;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public TokenType getType() {
		return type;
	}

	@Override
	public TokenFormat getFormat() {
		return format;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public Instant getIssuedAt() {
		return issuedAt;
	}

	@Override
	public Instant getExpiresAt() {
		return expiresAt;
	}

	@Override
	public boolean isRevoked() {
		return false;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getSubject() {
		return subject;
	}

	public List<String> getRoles() {
		return roles;
	}

	public static class Builder {
		private TokenType type;
		private TokenFormat format;
		private Object payload;
		private Instant issuedAt;
		private Instant expiresAt;
		private String issuer;
		private String subject;
		private List<String> roles;

		public Builder type(TokenType type) {
			this.type = type;
			return this;
		}

		public Builder format(TokenFormat format) {
			this.format = format;
			return this;
		}

		public Builder payload(Object payload) {
			this.payload = payload;
			return this;
		}

		public Builder issuedAt(Instant issuedAt) {
			this.issuedAt = issuedAt;
			return this;
		}

		public Builder expiresAt(Instant expiresAt) {
			this.expiresAt = expiresAt;
			return this;
		}

		public Builder issuer(String issuer) {
			this.issuer = issuer;
			return this;
		}

		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder roles(List<String> roles) {
			this.roles = roles;
			return this;
		}

		public JwtTokenData build() {
			return new JwtTokenData(this);
		}
	}
}
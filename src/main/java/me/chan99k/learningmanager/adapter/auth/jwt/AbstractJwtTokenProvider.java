package me.chan99k.learningmanager.adapter.auth.jwt;

import me.chan99k.learningmanager.adapter.auth.TokenRevocationProvider;

/**
 * JWT 토큰 관리를 위한 추상 클래스
 */
public abstract class AbstractJwtTokenProvider<T> {

	private final TokenRevocationProvider<T> revocationProvider;

	public AbstractJwtTokenProvider(TokenRevocationProvider<T> revocationProvider) {
		this.revocationProvider = revocationProvider;
	}

	public void revokeToken(String token) {
		revocationProvider.revokeToken(token);
	}

	protected boolean isRevoked(String token) {
		return revocationProvider.isRevoked(token);
	}

	protected abstract T getValidator();
}
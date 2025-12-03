package me.chan99k.learningmanager.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RevokeTokenService implements RevokeToken {

	private final RefreshTokenRepository refreshTokenRepository;

	public RevokeTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Override
	public void revoke(Request request) {
		refreshTokenRepository.revokeByToken(request.token());
	}
}

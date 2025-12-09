package me.chan99k.learningmanager.authentication;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RevokeAllTokensService implements RevokeAllTokens {

	private final RefreshTokenRepository refreshTokenRepository;

	public RevokeAllTokensService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Override
	public void revokeAll(Long memberId) {
		refreshTokenRepository.revokeAllByMemberId(memberId);
	}
}
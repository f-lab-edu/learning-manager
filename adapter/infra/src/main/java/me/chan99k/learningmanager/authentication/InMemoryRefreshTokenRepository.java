package me.chan99k.learningmanager.authentication;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
	private final Map<String, RefreshToken> tokenStore = new ConcurrentHashMap<>();
	private final Map<Long, String> memberTokenIndex = new ConcurrentHashMap<>();

	@Override
	public RefreshToken save(RefreshToken refreshToken) {
		String existingToken = memberTokenIndex.get(refreshToken.getMemberId());
		if (existingToken != null) {
			tokenStore.remove(existingToken);
		}

		tokenStore.put(refreshToken.getToken(), refreshToken);
		memberTokenIndex.put(refreshToken.getMemberId(), refreshToken.getToken());

		return refreshToken;
	}

	@Override
	public Optional<RefreshToken> findByToken(String token) {
		return Optional.ofNullable(tokenStore.get(token));
	}

	@Override
	public void revokeAllByMemberId(Long memberId) {
		String token = memberTokenIndex.remove(memberId);
		if (token != null) {
			RefreshToken refreshToken = tokenStore.get(token);
			if (refreshToken != null) {
				refreshToken.revoke();
			}
		}
	}

	@Override
	public void revokeByToken(String token) {
		RefreshToken refreshToken = tokenStore.get(token);
		if (refreshToken != null) {
			refreshToken.revoke();
			memberTokenIndex.remove(refreshToken.getMemberId());
		}
	}

	@Override
	public int deleteExpiredTokens() {
		int count = 0;
		var iterator = tokenStore.entrySet().iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			RefreshToken token = entry.getValue();
			if (token.isExpired() || token.isRevoked()) {
				memberTokenIndex.remove(token.getMemberId());
				iterator.remove();
				count++;
			}
		}
		return count;
	}
}

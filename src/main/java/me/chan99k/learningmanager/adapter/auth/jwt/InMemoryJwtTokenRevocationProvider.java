package me.chan99k.learningmanager.adapter.auth.jwt;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import me.chan99k.learningmanager.adapter.auth.TokenRevocationProvider;

@Component("memoryJwtTokenRevocationProvider")
public class InMemoryJwtTokenRevocationProvider implements TokenRevocationProvider<SecretKey> {

	private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

	@Override
	public void revokeToken(String token) {
		revokedTokens.add(token);
	}

	@Override
	public boolean isRevoked(String token) {
		return revokedTokens.contains(token);
	}

	@Override
	public void cleanup(SecretKey validator) {
		revokedTokens.removeIf(token -> {
			try {
				Jwts.parser()
					.verifyWith(validator)
					.build()
					.parseSignedClaims(token);
				return false; // 아직 유효함 - 무효화 리스트에 유지
			} catch (Exception e) {
				return true; // 만료됨 - 무효화 리스트에서 제거
			}
		});
	}

}

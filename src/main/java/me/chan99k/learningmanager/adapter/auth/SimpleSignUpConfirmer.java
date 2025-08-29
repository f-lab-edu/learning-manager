package me.chan99k.learningmanager.adapter.auth;

import java.time.Duration;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.SignUpConfirmer;

@Component
public class SimpleSignUpConfirmer
	extends AbstractTokenManager<SimpleSignUpConfirmer.ActivationData>
	implements SignUpConfirmer {
	@Override
	public String generateAndStoreToken(Long memberId, String email, Duration expiration) {
		var data = new ActivationData(memberId, email);
		return super.generateAndStoreToken(data, expiration);
	}

	@Override
	public boolean validateToken(String token) {
		return super.validateToken(token);
	}

	@Override
	public Long getMemberIdByToken(String token) {
		var data = super.getDataByToken(token);

		return data != null ? data.memberId() : null;
	}

	@Override
	public void removeToken(String token) {
		super.removeToken(token);
	}

	public record ActivationData(Long memberId, String email) {
	}

}

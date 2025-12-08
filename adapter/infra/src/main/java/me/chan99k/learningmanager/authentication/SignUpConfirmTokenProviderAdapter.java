package me.chan99k.learningmanager.authentication;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;

@Component
public class SignUpConfirmTokenProviderAdapter implements SignUpConfirmTokenProvider {

	private static final int TOKEN_BYTE_LENGTH = 24;

	private final InMemorySignUpConfirmTokenRepository tokenRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	public SignUpConfirmTokenProviderAdapter(InMemorySignUpConfirmTokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	@Override
	public String createAndStoreToken(String email) {
		String token = generateSecureToken();
		tokenRepository.save(token, email);
		return token;
	}

	@Override
	public String validateAndGetEmail(String token) {
		if (tokenRepository.isExpired(token)) {
			throw new DomainException(MemberProblemCode.EXPIRED_ACTIVATION_TOKEN);
		}

		return tokenRepository.findEmailByToken(token)
			.orElseThrow(() -> new DomainException(MemberProblemCode.INVALID_ACTIVATION_TOKEN));
	}

	@Override
	public void removeToken(String token) {
		tokenRepository.delete(token);
	}

	@Override
	public boolean isValid(String token) {
		return tokenRepository.existsAndNotExpired(token);
	}

	private String generateSecureToken() {
		byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}

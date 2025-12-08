package me.chan99k.learningmanager.authentication;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.MemberProblemCode;

@Component
public class PasswordResetTokenProviderAdapter implements PasswordResetTokenProvider {

	private static final int TOKEN_BYTE_LENGTH = 24;

	private final InMemoryPasswordResetTokenRepository tokenRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	public PasswordResetTokenProviderAdapter(
		InMemoryPasswordResetTokenRepository inMemoryPasswordResetTokenRepository) {
		this.tokenRepository = inMemoryPasswordResetTokenRepository;
	}

	@Override
	public String createAndStoreToken(Email email) {
		String token = generateSecureToken();
		tokenRepository.save(token, email);
		return token;
	}

	@Override
	public boolean validateResetToken(String token) {
		return tokenRepository.existsAndNotExpired(token);
	}

	@Override
	public String getEmailFromResetToken(String token) {
		return tokenRepository.findEmailByToken(token)
			.orElseThrow(() -> new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN));
	}

	@Override
	public void invalidateAfterUse(String token) {
		tokenRepository.delete(token);
	}

	private String generateSecureToken() {
		byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

}

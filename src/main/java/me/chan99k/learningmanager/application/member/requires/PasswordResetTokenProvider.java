package me.chan99k.learningmanager.application.member.requires;

import me.chan99k.learningmanager.domain.member.Email;

public interface PasswordResetTokenProvider {
	String createResetToken(String email);

	boolean validateResetToken(String token);

	Email getEmailFromResetToken(String token);

	void invalidateAfterUse(String token);
}
package me.chan99k.learningmanager.domain.member;

public interface EmailSender {
	void sendSignUpConfirmEmail(String email, String token);

	void sendPasswordResetEmail(String email, String token);
}

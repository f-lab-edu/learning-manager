package me.chan99k.learningmanager.member;

public interface PasswordResetVerification {

	Response verifyResetToken(Request request);

	record Request(String token) {
	}

	record Response(boolean tokenValid, String email) {

	}
}

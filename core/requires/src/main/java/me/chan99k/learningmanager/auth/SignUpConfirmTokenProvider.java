package me.chan99k.learningmanager.auth;

public interface SignUpConfirmTokenProvider {

	String createAndStoreToken(String email);

	String validateAndGetEmail(String token);

	void removeToken(String token);

	boolean isValid(String token);
}

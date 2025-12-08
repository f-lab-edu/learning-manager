package me.chan99k.learningmanager.authentication;

public interface SignUpConfirmTokenProvider {

	String createAndStoreToken(String email);

	String validateAndGetEmail(String token);

	void removeToken(String token);

	boolean isValid(String token);
}

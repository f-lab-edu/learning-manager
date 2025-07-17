package me.chan99k.learningmanager.domain.auth;

public interface PasswordEncoder {
	String encode(String rawString);

	boolean match(String rawString, String encoded);
}

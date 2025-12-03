package me.chan99k.learningmanager.auth;

public interface PasswordEncoder {

	String encode(String rawPassword);

	boolean matches(String rawPassword, String encodedPassword);
}

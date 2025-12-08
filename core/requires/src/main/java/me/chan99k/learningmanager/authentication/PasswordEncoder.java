package me.chan99k.learningmanager.authentication;

public interface PasswordEncoder {

	String encode(String rawPassword);

	boolean matches(String rawPassword, String encodedPassword);
}

package me.chan99k.learningmanager.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoder {

	private final BCryptPasswordEncoder delegate;

	public BcryptPasswordEncoderAdapter() {
		this.delegate = new BCryptPasswordEncoder();
	}

	@Override
	public String encode(String rawPassword) {
		return delegate.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		return delegate.matches(rawPassword, encodedPassword);
	}
}
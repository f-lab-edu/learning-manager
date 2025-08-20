package me.chan99k.learningmanager.adapter.auth;

import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Component
@Primary
public class BcryptPasswordEncoder implements PasswordEncoder {

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public BcryptPasswordEncoder() {
		this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
	}

	@Override
	public String encode(String rawString) {
		return bCryptPasswordEncoder.encode(rawString);
	}

	@Override
	public boolean match(String rawString, String encoded) {
		return bCryptPasswordEncoder.matches(rawString, encoded);
	}
}

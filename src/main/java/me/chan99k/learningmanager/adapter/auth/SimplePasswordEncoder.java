package me.chan99k.learningmanager.adapter.auth;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Component
public class SimplePasswordEncoder implements PasswordEncoder {
	// TODO :: Spring security의 BCryptPasswordEncoder 로 변경 필요
	@Override
	public String encode(String rawString) {
		return rawString.toUpperCase();
	}

	@Override
	public boolean matches(String rawString, String encoded) {
		return this.encode(rawString).equals(encoded);
	}
}

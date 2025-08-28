package me.chan99k.learningmanager.adapter.auth;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Component
@Primary
public class BcryptPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(String rawString) {
		return BCrypt.hashpw(rawString, BCrypt.gensalt());
	}

	@Override
	public boolean matches(String rawString, String encoded) {
		return BCrypt.checkpw(rawString, encoded);
	}
}

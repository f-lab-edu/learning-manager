package me.chan99k.learningmanager.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomUserDetails extends User {

	private final Long memberId;

	public CustomUserDetails(
		Long memberId,
		String email,
		Collection<? extends GrantedAuthority> authorities
	) {
		super(email, "", authorities);
		this.memberId = memberId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getEmail() {
		return getUsername();
	}
}

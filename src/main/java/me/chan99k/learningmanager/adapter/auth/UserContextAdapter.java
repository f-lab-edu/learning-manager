package me.chan99k.learningmanager.adapter.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.common.exception.AuthenticationException;

@Component
public class UserContextAdapter implements UserContext {

	@Override
	public Long getCurrentMemberId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
		}

		if (authentication.getPrincipal() instanceof Jwt jwt) {
			String subject = jwt.getSubject();
			try {
				return Long.valueOf(subject);
			} catch (NumberFormatException e) {
				throw new AuthenticationException(AuthProblemCode.INVALID_TOKEN_SUBJECT);
			}
		}

		throw new AuthenticationException(AuthProblemCode.INVALID_AUTHENTICATION_INFO);
	}

	@Override
	public boolean isAuthenticated() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.isAuthenticated();
	}
}
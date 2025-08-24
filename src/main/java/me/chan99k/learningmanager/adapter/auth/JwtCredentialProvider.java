package me.chan99k.learningmanager.adapter.auth;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.CredentialProvider;
import me.chan99k.learningmanager.domain.member.Member;

@Component
@Primary
public class JwtCredentialProvider implements CredentialProvider {
	private final JwtTokenProvider jwtTokenProvider;

	public JwtCredentialProvider(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public String issueCredential(Member member) {
		return jwtTokenProvider.createToken(member.getId());
	}
}

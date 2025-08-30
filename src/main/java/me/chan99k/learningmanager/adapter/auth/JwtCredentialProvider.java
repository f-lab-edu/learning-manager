package me.chan99k.learningmanager.adapter.auth;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.CredentialProvider;
import me.chan99k.learningmanager.domain.member.Member;

@Component
@Primary
public class JwtCredentialProvider implements CredentialProvider {
	private final AccessTokenProvider<Long> accessTokenProvider;

	public JwtCredentialProvider(AccessTokenProvider<Long> accessTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
	}

	@Override
	public String issueCredential(Member member) {
		return accessTokenProvider.createAccessToken(member.getId());
	}
}

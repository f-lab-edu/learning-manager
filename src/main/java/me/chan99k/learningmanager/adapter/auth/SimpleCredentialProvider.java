package me.chan99k.learningmanager.adapter.auth;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.CredentialProvider;
import me.chan99k.learningmanager.domain.member.Member;

@Component
public class SimpleCredentialProvider implements CredentialProvider {
	// TODO :: 실제 자격 증명 정보 제공자로 변경이 필요함
	@Override
	public String issueCredential(Member member) {
		return "access_token_" + member.getId() + "_" + System.currentTimeMillis();
	}
}

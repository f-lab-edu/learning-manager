package me.chan99k.learningmanager.domain.member;

public interface CredentialProvider {
	String issueCredential(Member member);
}

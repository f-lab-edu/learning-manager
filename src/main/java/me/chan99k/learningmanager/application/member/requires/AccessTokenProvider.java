package me.chan99k.learningmanager.application.member.requires;

public interface AccessTokenProvider {
	String generateAccessToken(Long memberId, String email);
}
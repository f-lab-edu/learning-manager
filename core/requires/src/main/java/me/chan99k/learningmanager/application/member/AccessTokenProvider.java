package me.chan99k.learningmanager.application.member;

public interface AccessTokenProvider {
	String generateAccessToken(Long memberId, String email);
}
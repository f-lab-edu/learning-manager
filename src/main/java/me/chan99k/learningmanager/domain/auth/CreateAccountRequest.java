package me.chan99k.learningmanager.domain.auth;

public record CreateAccountRequest(
	String email, String password, Long memberId
) {
}

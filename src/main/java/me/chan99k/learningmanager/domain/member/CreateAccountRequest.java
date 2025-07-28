package me.chan99k.learningmanager.domain.member;

public record CreateAccountRequest(
	String email, String password, Long memberId
) {
}

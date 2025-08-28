package me.chan99k.learningmanager.application.member.provides;

/**
 * [P1] 비밀번호 변경
 */
public interface AccountPasswordChange {
	Response changePassword(Request request);

	record Request(String email, String newPassword) {
	}

	record Response() {
	}
}

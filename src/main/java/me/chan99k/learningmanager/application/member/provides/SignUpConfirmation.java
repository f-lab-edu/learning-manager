package me.chan99k.learningmanager.application.member.provides;

public interface SignUpConfirmation {
	void activateSignUpMember(SignUpConfirmation.Request request);

	record Request(String token) {
	}
}

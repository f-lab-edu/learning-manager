package me.chan99k.learningmanager.application.auth;

public interface UserContext {
	Long getCurrentMemberId();

	boolean isAuthenticated();
}
package me.chan99k.learningmanager.application.auth.requires;

public interface UserContext {
	Long getCurrentMemberId();

	boolean isAuthenticated();
}
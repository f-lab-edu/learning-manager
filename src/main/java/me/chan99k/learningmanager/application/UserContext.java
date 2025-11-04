package me.chan99k.learningmanager.application;

public interface UserContext {
	Long getCurrentMemberId();

	boolean isAuthenticated();
}
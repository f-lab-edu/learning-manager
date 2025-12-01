package me.chan99k.learningmanager.auth.token;

import java.time.Instant;

public interface TokenData {
	String getSubject();

	Long getMemberId();

	String getValue();

	Instant getExpiresAt();

	boolean isExpired();

	TokenType getType();

	TokenFormat getFormat();
}
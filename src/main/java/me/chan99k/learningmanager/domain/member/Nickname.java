package me.chan99k.learningmanager.domain.member;

import java.util.UUID;

public record Nickname(String value) {
	public static Nickname generateWithUUID() {
		return new Nickname("user_" + UUID.randomUUID().toString().substring(0, 8));
	}
}
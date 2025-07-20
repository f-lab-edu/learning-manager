package me.chan99k.learningmanager.domain.member;

import jakarta.persistence.Embeddable;

@Embeddable
public record Nickname(String value) {
	public static Nickname generateNickname(NicknameGenerator nicknameGenerator) {
		return new Nickname(nicknameGenerator.generate());
	}
}
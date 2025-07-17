package me.chan99k.learningmanager.domain.member;

public enum SystemRole {
	ADMIN("시스템 관리자"), MEMBER("회원");

	public final String value;

	SystemRole(String value) {
		this.value = value;
	}
}

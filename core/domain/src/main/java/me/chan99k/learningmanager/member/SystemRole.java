package me.chan99k.learningmanager.member;

public enum SystemRole {
	ADMIN("시스템 관리자", 3),
	MEMBER("회원", 0),
	SUPERVISOR("감독관", 2),
	OPERATOR("운영자", 1),
	REGISTRAR("학적 담당", 1),
	AUDITOR("감사관", 1);

	public final String description;

	public final int level;

	SystemRole(String description, int level) {
		this.description = description;
		this.level = level;
	}
}

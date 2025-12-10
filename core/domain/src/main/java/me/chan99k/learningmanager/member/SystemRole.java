package me.chan99k.learningmanager.member;

public enum SystemRole {
	ADMIN("시스템 관리자"),
	MEMBER("회원"),
	SUPERVISOR("감독관"),
	OPERATOR("운영자"),
	REGISTRAR("학적 담당"),
	AUDITOR("감사관");

	public final String value;

	SystemRole(String value) {
		this.value = value;
	}
}

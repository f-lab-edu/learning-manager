package me.chan99k.learningmanager.domain.member;

public enum MemberStatus {
	ACTIVE("활동 중"),
	INACTIVE("휴면 중"),
	BANNED("이용 정지"),
	WITHDRAWN("탈퇴");

	public final String value;

	MemberStatus(String value) {
		this.value = value;
	}
}

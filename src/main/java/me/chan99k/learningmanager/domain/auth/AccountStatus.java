package me.chan99k.learningmanager.domain.auth;

public enum AccountStatus {
	PENDING("대기"), ACTIVE("활성"), INACTIVE("휴면");

	public final String value;

	AccountStatus(String value) {
		this.value = value;
	}
}

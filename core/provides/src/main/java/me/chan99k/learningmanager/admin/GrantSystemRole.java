package me.chan99k.learningmanager.admin;

import me.chan99k.learningmanager.member.SystemRole;

public interface GrantSystemRole {

	void grant(Request request);

	record Request(
		Long memberId,
		SystemRole role
	) {
	}
}

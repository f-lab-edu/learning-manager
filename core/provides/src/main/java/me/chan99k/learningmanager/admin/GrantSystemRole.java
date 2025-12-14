package me.chan99k.learningmanager.admin;

import me.chan99k.learningmanager.member.SystemRole;

public interface GrantSystemRole {

	void grant(Long grantedBy, Request request);

	record Request(
		Long memberId,
		SystemRole role
	) {
	}
}

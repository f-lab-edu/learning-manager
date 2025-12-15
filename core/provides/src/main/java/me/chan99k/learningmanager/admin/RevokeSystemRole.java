package me.chan99k.learningmanager.admin;

import me.chan99k.learningmanager.member.SystemRole;

public interface RevokeSystemRole {

	void revoke(Request request);

	record Request(
		Long memberId,
		SystemRole role,
		Long performedBy,
		String reason
	) {
	}
}

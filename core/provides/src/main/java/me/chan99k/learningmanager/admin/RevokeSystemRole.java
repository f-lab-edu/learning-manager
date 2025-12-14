package me.chan99k.learningmanager.admin;

import me.chan99k.learningmanager.member.SystemRole;

public interface RevokeSystemRole {

	void revoke(Long revokedBy, Request request);

	record Request(
		Long memberId,
		SystemRole role
	) {
	}
}

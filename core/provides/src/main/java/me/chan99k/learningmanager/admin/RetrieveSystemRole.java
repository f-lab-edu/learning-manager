package me.chan99k.learningmanager.admin;

import java.util.Set;

import me.chan99k.learningmanager.member.SystemRole;

public interface RetrieveSystemRole {

	Response retrieve(Long memberId);

	record Response(Long memberId, Set<SystemRole> roles) {
	}
}

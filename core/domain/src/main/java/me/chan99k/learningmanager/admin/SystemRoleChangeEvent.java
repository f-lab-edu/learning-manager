package me.chan99k.learningmanager.admin;

import java.time.Instant;

import me.chan99k.learningmanager.member.SystemRole;

public sealed interface SystemRoleChangeEvent permits SystemRoleChangeEvent.Granted, SystemRoleChangeEvent.Revoked {

	Long memberId();

	SystemRole role();

	Long performedBy();

	Instant performedAt();

	String reason();

	record Granted(
		Long memberId,
		SystemRole role,
		Long performedBy,
		Instant performedAt,
		String reason
	) implements SystemRoleChangeEvent {
	}

	record Revoked(
		Long memberId,
		SystemRole role,
		Long performedBy,
		Instant performedAt,
		String reason
	) implements SystemRoleChangeEvent {
	}
}

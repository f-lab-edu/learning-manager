package me.chan99k.learningmanager.domain.member;

import java.time.Instant;

public record MemberStatusHistory(
	Long id,
	Long memberId,
	MemberStatus status,
	String reason,
	Instant changedAt
) {
}

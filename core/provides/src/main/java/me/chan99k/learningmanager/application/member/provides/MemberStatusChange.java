package me.chan99k.learningmanager.application.member.provides;

import me.chan99k.learningmanager.domain.member.MemberStatus;

public interface MemberStatusChange {

	void changeStatus(Request request);

	record Request(
		Long memberId,
		MemberStatus status
	) {
	}
}

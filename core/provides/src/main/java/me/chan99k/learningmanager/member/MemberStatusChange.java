package me.chan99k.learningmanager.member;

public interface MemberStatusChange {

	void changeStatus(Request request);

	record Request(
		Long memberId,
		MemberStatus status
	) {
	}
}

package me.chan99k.learningmanager.application.member.provides;

/**
 * [P1] 프로필 수정
 */
public interface MemberProfileUpdate {
	MemberProfileUpdate.Response updateProfile(Long memberId, MemberProfileUpdate.Request request);

	record Request(
		String profileImageUrl,
		String selfIntroduction
	) {
	}

	record Response(
		Long memberId
	) {
	}
}

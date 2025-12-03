package me.chan99k.learningmanager.member;

import jakarta.validation.constraints.NotBlank;

public interface MemberProfileRetrieval {

	MemberProfileRetrieval.Response getProfile(Long memberId);

	MemberProfileRetrieval.Response getPublicProfile(String nickname);

	record Request(
		@NotBlank(message = "닉네임 입력값은 필수입니다")
		String nickname
	) {
	}

	// TODO :: 공개 API 와 회원용 API에서 제공하는 프로필 정보가 많이 달라지게 되면 DTO를 분리해야 함
	record Response(
		String profileImageUrl,
		String selfIntroduction
	) {
	}
}

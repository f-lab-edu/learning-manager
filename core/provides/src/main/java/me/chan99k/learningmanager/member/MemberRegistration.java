package me.chan99k.learningmanager.member;

import jakarta.validation.constraints.NotBlank;

/**
 * [P0] 사용자 가입
 */
public interface MemberRegistration {

	MemberRegistration.Response register(MemberRegistration.Request request);

	record Request(
		@NotBlank(message = "[System] 이메일은 회원가입에 필수요소 입니다.")
		String email,
		@NotBlank(message = "[System] 비밀번호는 회원가입에 필수요소 입니다.")
		String rawPassword
	) {
	}

	record Response(Long memberId) {
	}
}

package me.chan99k.learningmanager.member;

/**
 * 회원 가입 확인 인터페이스.
 * 이메일 인증 등을 통한 회원 가입 활성화 기능을 정의합니다.
 */
public interface SignUpConfirmation {

	/**
	 * 가입 확인 토큰을 통해 회원을 활성화합니다.
	 *
	 * @param request 활성화 요청 (토큰 포함)
	 */
	void activateSignUpMember(Request request);

	/**
	 * 회원 가입 확인 요청 레코드
	 *
	 * @param token 가입 확인 토큰
	 */
	record Request(String token) {
	}
}

package me.chan99k.learningmanager.application.member.provides;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * [P1] 비밀번호 재설정
 *  주요 액터(Actor): 사용자(게스트)
 * <p>
 *  사전 조건: 사용자가 자신의 비밀번호를 잊었다.
 * <p>
 *  성공 시나리오:
 * <p>
 *  1. 사용자가 '비밀번호 찾기' 페이지에서 자신의 가입 이메일을 입력한다.
 *  2. 시스템은 해당 이메일로 비밀번호를 재설정할 수 있는 링크를 발송한다.
 *  3. 사용자가 이메일의 링크를 클릭하여 비밀번호 재설정 페이지로 이동한다.
 *  4. 사용자가 새 비밀번호를 입력하고 '변경'을 요청한다.
 *  5. 시스템은 새 비밀번호를 암호화하여 Account에 저장하고 완료를 알린다.
 *  실패 시나리오:
 * <p>
 *  - (가입되지 않은 이메일): 시스템에 존재하지 않는 이메일일 경우, "가입되지 않은 이메일입니다."라는 메시지를 반환한다.
 *  - (유효하지 않은 링크): 재설정 링크가 만료되었거나 유효하지 않을 경우, 오류 메시지를 반환한다.
 *  <p>
 *  연관 도메인: Notification, Account (서브 도메인)
 */
public interface AccountPasswordReset {
	RequestResetResponse requestReset(RequestResetRequest request);

	ConfirmResetResponse confirmReset(ConfirmResetRequest request);

	boolean validatePasswordResetToken(@Valid String token);

	// 재설정 요청
	record RequestResetRequest(
		@NotBlank(message = "가입시 사용한 이메일 값은 필수입니다")
		String email
	) {
	}

	/**
	 * 가입시 사용한 이메일: {} 로 비밀번호 재설정 메일을 발송했다 라는 메시지를 반환
	 * @param message
	 */
	record RequestResetResponse(String message) {
	}

	// 새 비밀번호 설정
	record ConfirmResetRequest(
		@NotBlank(message = "유효한 토큰은 필수입니다")
		String token,
		@NotBlank(message = "변경할 새 비밀번호 값은 필수입니다")
		String newPassword
	) {
	}

	record ConfirmResetResponse() {
	}
}



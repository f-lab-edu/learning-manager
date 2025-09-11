package me.chan99k.learningmanager.domain.attendance;

import me.chan99k.learningmanager.common.exception.ProblemCode;

public enum AttendanceProblemCode implements ProblemCode {
	SESSION_ID_REQUIRED("DAL001", "[System] 출석을 기록할 세션은 필수입니다."),
	MEMBER_ID_REQUIRED("DAL002", "[System] 출석을 기록할 회원은 필수입니다."),
	CANNOT_REASSIGN_ID("DAL003", "[System] ID는 최초 한 번만 설정 가능합니다."),
	ALREADY_CHECKED_IN("DAL100", "[System] 이미 입실이 완료되었습니다. "),
	NOT_CHECKED_IN("DAL101", "[System] 입실 상태가 아닙니다."),
	CHECK_OUT_TIME_BEFORE_CHECK_IN_TIME("DAL200", "[System] 퇴실 시간은 입실 시간보다 빠를 수 없습니다."),
	;

	private final String code;
	private final String message;

	AttendanceProblemCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}

package me.chan99k.learningmanager.attendance;

import me.chan99k.learningmanager.exception.ProblemCode;

public enum AttendanceProblemCode implements ProblemCode {
	SESSION_ID_REQUIRED("DAL001", "[System] 출석을 기록할 세션은 필수입니다."),
	MEMBER_ID_REQUIRED("DAL002", "[System] 출석을 기록할 회원은 필수입니다."),
	CANNOT_REASSIGN_ID("DAL003", "[System] ID는 최초 한 번만 설정 가능합니다."),
	ONLY_ROOT_SESSION_ALLOWED("DAL004", "[System] 루트 세션이 아닙니다."),
	ATTENDANCE_ID_REQUIRED("DAL005", "[System] 출석 기록 ID는 필수입니다."),

	ALREADY_CHECKED_IN("DAL100", "[System] 이미 입실이 완료되었습니다. "),
	NOT_CHECKED_IN("DAL101", "[System] 입실 상태가 아닙니다."),
	CHECK_OUT_TIME_BEFORE_CHECK_IN_TIME("DAL200", "[System] 퇴실 시간은 입실 시간보다 빠를 수 없습니다."),

	// QR 코드 관련
	INVALID_QR_TOKEN("DAL300", "[System] QR 코드 토큰 검증에 실패하였습니다."),

	PENDING_REQUEST_EXISTS("DAL400", "[System] 이미 대기 중인 수정 요청이 있습니다."),
	NO_PENDING_REQUEST("DAL401", "[System] 대기 중인 수정 요청이 없습니다."),
	SAME_STATUS_REQUEST("DAL402", "[System] 현재 상태와 동일한 상태로 변경할 수 없습니다."),
	CORRECTION_REASON_REQUIRED("DAL403", "[System] 출석 상태 수정 요청에 대한 사유는 필수입니다."),
	REJECTION_REASON_REQUIRED("DAL404", "[System] 출석 상태 수정 요청 거절에 대한 사유는 필수 입니다."),

	ATTENDANCE_NOT_FOUND("DAL500", "[System] 해당 출석 기록을 찾을 수 없습니다"),
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

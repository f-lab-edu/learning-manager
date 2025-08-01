package me.chan99k.learningmanager.domain.member;

import me.chan99k.learningmanager.common.exception.ProblemCode;

public enum MemberProblemCode implements ProblemCode {

	MEMBER_NOT_GENERAL("DML001", "[System] 일반 회원만 관리자로 승격될 수 있습니다."),
	MEMBER_NOT_ADMIN("DML002", "[System] 관리자만 일반 회원으로 강등될 수 있습니다."),
	MEMBER_ALREADY_INACTIVE("DML003", "[System] 이미 휴면 상태의 회원입니다."),
	MEMBER_NOT_INACTIVE("DML004", "[System] 휴면 상태의 회원만 활성화할 수 있습니다."),
	MEMBER_ALREADY_WITHDRAWN("DML005", "[System] 이미 탈퇴한 회원입니다."),
	MEMBER_NOT_ACTIVE("DML006", "[System] 활동 중인 회원만 이용 정지될 수 있습니다."),
	MEMBER_NOT_BANNED("DML007", "[System] 이용 정지 상태의 회원만 해제될 수 있습니다."),

	ACCOUNT_MEMBER_REQUIRED("DML008", "[System] 계정은 반드시 멤버에 속해야 합니다."),
	ACCOUNT_NOT_PENDING_OR_INACTIVE("DML009", "[System] 활성 대기/비활성 상태의 계정이 아닙니다."),
	ACCOUNT_NOT_ACTIVE("DML010", "[System] 활성 상태의 계정이 아닙니다."),

	INVALID_EMAIL_FORMAT("DML011", "[System] 유효하지 않은 이메일 형식입니다."),

	PASSWORD_LENGTH_INVALID("DML012", "[System] 비밀번호는 최소 8자 이상, 64자 이하여야 합니다."),
	PASSWORD_NO_LOWERCASE("DML013", "[System] 비밀번호에는 소문자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_NO_UPPERCASE("DML014", "[System] 비밀번호에는 대문자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_NO_DIGIT("DML015", "[System] 비밀번호에는 숫자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_NO_SPECIAL_CHAR("DML016", "[System] 비밀번호에는 특수문자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_CONTAINS_WHITESPACE("DML017", "[System] 비밀번호에 공백을 포함할 수 없습니다.");

	private final String code;
	private final String message;

	MemberProblemCode(String code, String message) {
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

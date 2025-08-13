package me.chan99k.learningmanager.domain.member;

import me.chan99k.learningmanager.common.exception.ProblemCode;

public enum MemberProblemCode implements ProblemCode {

	MEMBER_NOT_GENERAL("DML001", "[System] 일반 회원만 관리자로 승격될 수 있습니다."),
	MEMBER_NOT_ADMIN("DML002", "[System] 관리자만 일반 회원으로 강등될 수 있습니다."),
	MEMBER_ALREADY_INACTIVE("DML003", "[System] 이미 휴면 상태의 회원입니다."),
	MEMBER_NOT_PENDING_OR_INACTIVE("DML004", "[System] 가입 대기/휴면 상태의 회원만 활성화할 수 있습니다."),
	MEMBER_ALREADY_WITHDRAWN("DML005", "[System] 이미 탈퇴한 회원입니다."),
	MEMBER_NOT_ACTIVE("DML006", "[System] 활동 중인 회원만 이용 정지될 수 있습니다."),
	MEMBER_NOT_BANNED("DML007", "[System] 이용 정지 상태의 회원만 해제될 수 있습니다."),
	MEMBER_NOT_FOUND("DML026", "[System] 해당 회원이 존재하지 않습니다.."),

	ACCOUNT_MEMBER_REQUIRED("DML008", "[System] 계정은 반드시 멤버에 속해야 합니다."),
	ACCOUNT_NOT_PENDING_OR_INACTIVE("DML009", "[System] 활성 대기/비활성 상태의 계정이 아닙니다."),
	ACCOUNT_ALREADY_ACTIVATED("DML026", "[System] 이미 활성화된 계정입니다."),
	ACCOUNT_NOT_ACTIVE("DML010", "[System] 활성 상태의 계정이 아닙니다."),
	ACCOUNT_ID_REQUIRED("DML010-1", "[System] 계정 ID는 null일 수 없습니다."),
	ACCOUNT_NOT_FOUND("DML027", "[System] 해당 계정이 존재하지 않습니다"),
	// TODO :: 예외 메시지들을 종류별로 묶어 놓았는데, 식별 코드를 +1 씩 증가 시켜서 사용 하다보니 계층 구조적으로 관리가 안됨 -> 정책 수정 필요 -> 애플리케이션 시작시 code를 generate 하는 방법?
	INVALID_EMAIL_FORMAT("DML011", "[System] 유효하지 않은 이메일 형식입니다."),

	PASSWORD_LENGTH_INVALID("DML012", "[System] 비밀번호는 최소 8자 이상, 64자 이하여야 합니다."),
	PASSWORD_NO_LOWERCASE("DML013", "[System] 비밀번호에는 소문자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_NO_UPPERCASE("DML014", "[System] 비밀번호에는 대문자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_NO_DIGIT("DML015", "[System] 비밀번호에는 숫자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_NO_SPECIAL_CHAR("DML016", "[System] 비밀번호에는 특수문자가 최소 1개 이상 포함되어야 합니다."),
	PASSWORD_CONTAINS_WHITESPACE("DML017", "[System] 비밀번호에 공백을 포함할 수 없습니다."),
	CANNOT_FOUND_ACCOUNT("DML018", "[System] 유효한 계정 정보를 찾을 수 없습니다."),

	MEMBER_NICKNAME_REQUIRED("DML019", "[System] 닉네임은 비어 있을 수 없습니다."),
	MEMBER_NICKNAME_CONSTRAINTS_FOR_LENGTH("DML020", "[System] 닉네임은 %d자 이상 %d자 이하여야 합니다."),
	MEMBER_NICKNAME_CONSTRAINTS_FOR_CHARACTER("DML021", "[System] 닉네임은 한글, 영문, 숫자만 사용할 수 있습니다."),

	EMAIL_ALREADY_EXISTS("DML022", "[System] 이미 등록된 이메일입니다."),
	MEMBER_REGISTRATION_FAILED("DML023", "[System] 회원 등록에 실패했습니다."),

	INVALID_ACTIVATION_TOKEN("DML024", "[System] 유효하지 않은 회원 활성화 토큰입니다."),
	EXPIRED_TEST_ACTIVATION_TOKEN("DML025", "[System] 회원 활성화 토큰이 만료되었습니다.");

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

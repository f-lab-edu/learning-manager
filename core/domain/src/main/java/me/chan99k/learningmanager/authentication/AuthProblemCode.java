package me.chan99k.learningmanager.authentication;

import me.chan99k.learningmanager.exception.ProblemCode;

public enum AuthProblemCode implements ProblemCode {

	INVALID_CREDENTIALS("AUTH001", "[AUTH] 이메일 또는 비밀번호가 올바르지 않습니다."),

	// 토큰 관련
	INVALID_TOKEN("AUTH002", "[AUTH] 유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN("AUTH003", "[AUTH] 만료된 토큰입니다."),
	REVOKED_TOKEN("AUTH004", "[AUTH] 폐기된 토큰입니다."),
	TOKEN_NOT_FOUND("AUTH005", "[AUTH] 토큰을 찾을 수 없습니다."),

	UNSUPPORTED_GRANT_TYPE("AUTH006", "[AUTH] 지원하지 않는 grant_type입니다."),

	AUTHENTICATION_REQUIRED("AUTH007", "[AUTH] 인증이 필요합니다."),
	ACCESS_DENIED("AUTH008", "[AUTH] 접근 권한이 없습니다.");

	private final String code;
	private final String message;

	AuthProblemCode(String code, String message) {
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

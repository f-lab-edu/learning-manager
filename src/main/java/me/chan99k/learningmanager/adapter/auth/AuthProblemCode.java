package me.chan99k.learningmanager.adapter.auth;

import me.chan99k.learningmanager.common.exception.ProblemCode;

// TODO ::Auth 는 도메인의 관심사가 아니라 시스템의 관심사 이다. 모듈화 해서 도메인과 분리해야 한다.
public enum AuthProblemCode implements ProblemCode {
	FAILED_TO_AUTHENTICATE("DAL001", "[System] 인증에 실패하였습니다"),
	FAILED_TO_VALIDATE_TOKEN("DAL002", "[System] 토큰 유효성 검증에 실패하였습니다"),
	INVALID_TOKEN_SUBJECT("DAL003", "[System] 토큰의 subject 가 유효하지 않습니다"),
	AUTHENTICATION_REQUIRED("DAL004", "[System] 인증이 필요한 요청입니다"),
	MISSING_AUTHORIZATION_HEADER("DAL005", "[System] Authorization 헤더가 없습니다"),
	INVALID_AUTHORIZATION_HEADER("DAL006", "[System] Authorization 헤더 형식이 올바르지 않습니다"),
	EMPTY_BEARER_TOKEN("DAL007", "[System] Bearer 토큰이 비어있습니다");

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

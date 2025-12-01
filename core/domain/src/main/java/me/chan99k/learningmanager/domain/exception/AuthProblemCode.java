package me.chan99k.learningmanager.domain.exception;

public enum AuthProblemCode implements ProblemCode {
	// 인증 관련
	FAILED_TO_AUTHENTICATE("IAUTH100", "[System] 인증에 실패하였습니다"),
	FAILED_TO_VALIDATE_TOKEN("IAUTH101", "[System] 토큰 유효성 검증에 실패하였습니다"),
	INVALID_TOKEN_SUBJECT("IAUTH102", "[System] 토큰의 subject 가 유효하지 않습니다"),
	AUTHENTICATION_REQUIRED("IAUTH103", "[System] 인증이 필요한 요청입니다"),
	MISSING_AUTHORIZATION_HEADER("IAUTH104", "[System] Authorization 헤더가 없습니다"),
	INVALID_AUTHORIZATION_HEADER("IAUTH105", "[System] Authorization 헤더 형식이 올바르지 않습니다"),
	EMPTY_BEARER_TOKEN("IAUTH106", "[System] Bearer 토큰이 비어있습니다"),
	AUTHENTICATION_CONTEXT_NOT_FOUND("IAUTH107", "[System] 인증 컨텍스트를 찾을 수 없습니다."),
	INVALID_AUTHENTICATION_INFO("IAUTH108", "[System] 인증 정보가 올바르지 않습니다"),
	// 토큰 관련
	INVALID_TOKEN("IAUTH200", "[System] 유효하지 않은 토큰입니다"),
	INVALID_TOKEN_PURPOSE("IAUTH201", "[System] 토큰 용도가 올바르지 않습니다"),
	INVALID_TOKEN_CLAIMS("IAUTH202", "[System] 토큰 클레임이 올바르지 않습니다"),
	FAILED_TO_PARSE_TOKEN("IAUTH203", "[System] 토큰 파싱에 실패하였습니다"),
	FAILED_TO_PARSE_JWT("IAUTH204", "[System] JWT 토큰 파싱에 실패하였습니다"),

	// 인가 관련
	AUTHORIZATION_REQUIRED("AUTH400", "[System] 요청 동작에 대한 권한이 없습니다");

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
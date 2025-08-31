package me.chan99k.learningmanager.common.exception;

public class AuthException extends RuntimeException {
	private final ProblemCode problemCode;

	public AuthException(ProblemCode problemCode) {
		super(problemCode.getMessage());
		this.problemCode = problemCode;
	}

	public AuthException(ProblemCode problemCode, Throwable cause) {
		super(problemCode.getMessage(), cause);
		this.problemCode = problemCode;
	}

	public ProblemCode getProblemCode() {
		return problemCode;
	}
}

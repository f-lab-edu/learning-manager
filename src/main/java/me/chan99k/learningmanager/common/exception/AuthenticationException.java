package me.chan99k.learningmanager.common.exception;

public class AuthenticationException extends RuntimeException {
	private final ProblemCode problemCode;

	public AuthenticationException(ProblemCode problemCode) {
		super(problemCode.getMessage());
		this.problemCode = problemCode;
	}

	public AuthenticationException(ProblemCode problemCode, Throwable cause) {
		super(problemCode.getMessage(), cause);
		this.problemCode = problemCode;
	}

	public ProblemCode getProblemCode() {
		return problemCode;
	}
}
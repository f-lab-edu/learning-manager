package me.chan99k.learningmanager.common.exception;

public class AuthenticateException extends RuntimeException {
	private final ProblemCode problemCode;

	public AuthenticateException(ProblemCode problemCode) {
		super(problemCode.getMessage());
		this.problemCode = problemCode;
	}

	public AuthenticateException(ProblemCode problemCode, Throwable cause) {
		super(problemCode.getMessage(), cause);
		this.problemCode = problemCode;
	}

	public ProblemCode getProblemCode() {
		return problemCode;
	}
}

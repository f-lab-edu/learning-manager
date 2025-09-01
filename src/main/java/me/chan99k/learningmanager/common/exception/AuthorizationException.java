package me.chan99k.learningmanager.common.exception;

public class AuthorizationException extends RuntimeException {
	private final ProblemCode problemCode;

	public AuthorizationException(ProblemCode problemCode) {
		super(problemCode.getMessage());
		this.problemCode = problemCode;
	}

	public ProblemCode getProblemCode() {
		return problemCode;
	}
}
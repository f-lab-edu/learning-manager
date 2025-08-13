package me.chan99k.learningmanager.common.exception;

public class DomainException extends RuntimeException {
	private final ProblemCode problemCode;

	public DomainException(ProblemCode problemCode) {
		super(problemCode.getMessage());
		this.problemCode = problemCode;
	}

	public DomainException(ProblemCode problemCode, Throwable cause) {
		super(problemCode.getMessage(), cause);
		this.problemCode = problemCode;
	}

	public ProblemCode getProblemCode() {
		return problemCode;
	}
}
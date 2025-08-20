package me.chan99k.learningmanager.common.exception;

public class DomainException extends RuntimeException { // FIXME :: 도메인 예외인데 왜 밖에 정의해두고 도메인에서 가져다 쓰는가?
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
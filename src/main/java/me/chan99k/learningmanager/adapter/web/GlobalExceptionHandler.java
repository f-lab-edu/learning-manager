package me.chan99k.learningmanager.adapter.web;

import java.net.URI;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.common.exception.UnauthenticatedException;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * DomainException 을 가로챈다.
	 * @param e DomainException
	 * @return 400, ResponseEntity
	 */
	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ProblemDetail> handleDomainException(DomainException e) {
		if (e.getProblemCode() == MemberProblemCode.ACCOUNT_NOT_FOUND) { // 계정 없음 예외의 경우
			ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
			problem.setDetail(e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
		}

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			e.getProblemCode().getMessage()
		);

		// TODO :: 에러에 대한 문서로 연결 되면 좋음
		problemDetail.setType(URI.create("https://api.lm.com/errors/" + e.getProblemCode().getCode()));
		problemDetail.setTitle("Domain Error");
		problemDetail.setProperty("code", e.getProblemCode().getCode());

		return ResponseEntity.badRequest().body(problemDetail);
	}

	/**
	 * 어떤 컨트롤러에서든 validation 에러가 발생하여 MethodArgumentNotValidException 이 던져지면 이 메서드가 가로챈다.
	 *
	 * @param e Bean Validation(@NotBlank, @NotNull 등) 검증 실패 시 바인딩 과정에서 발생하는 예외
	 * @return 400, ResponseEntity
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.orElse("[System] 유효하지 않은 입력입니다."); // 기본 메시지

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			message
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/validation"));
		problemDetail.setTitle("Validation Error");
		problemDetail.setProperty("code", "VALIDATION_ERROR");

		return ResponseEntity.badRequest().body(problemDetail);
	}

	/**
	 * 어디에서든 IllegalArgumentException이 발생하면 이 메서드가 가로챈다.
	 * @param e IllegalArgumentException
	 * @return 400, ResponseEntity
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			e.getMessage()
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/invalid-argument"));
		problemDetail.setTitle("Invalid Argument");
		problemDetail.setProperty("code", "INVALID_ARGUMENT");

		return ResponseEntity.badRequest().body(problemDetail);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ProblemDetail> handleBadRequest(HttpMessageNotReadableException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

		problemDetail.setType(URI.create("https://api.lm.com/errors/invalid-json-request"));
		problemDetail.setTitle("Bad Request");
		problemDetail.setProperty("code", "INVALID JSON");

		return ResponseEntity.badRequest().body(problemDetail);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			e.getStatusCode(),
			e.getReason() != null ? e.getReason() : "Request processing failed"
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/" + e.getStatusCode().value()));
		problemDetail.setTitle(e.getStatusCode().toString());
		problemDetail.setProperty("code", e.getStatusCode().toString());

		return ResponseEntity.status(e.getStatusCode()).body(problemDetail);
	}

	@ExceptionHandler(UnauthenticatedException.class) //  TODO :: ResponseStatusException 과 비교하여 어떤 예외를 사용할 것인지 결정하여야 함
	public ResponseEntity<ProblemDetail> handleUnauthenticatedException(UnauthenticatedException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.UNAUTHORIZED,
			e.getMessage()
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/authentication"));
		problemDetail.setTitle("Unauthorized");
		problemDetail.setProperty("code", "UNAUTHORIZED"); // TODO :: ProblemCode 만들어 넣기

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
	}


	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleGeneralException(Exception e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"[System] 일시적인 서버 오류가 발생했습니다."
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/internal-server-error"));
		problemDetail.setTitle("Internal Server Error");
		problemDetail.setProperty("code", "INTERNAL_SERVER_ERROR");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
	}
}
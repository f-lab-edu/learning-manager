package me.chan99k.learningmanager.adapter.web;

import java.net.URI;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
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
			return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(createProblemJsonHeaders()).body(problem);
		}

		if (e.getProblemCode() == MemberProblemCode.EMAIL_ALREADY_EXISTS) { // 이메일 중복 예외의 경우
			ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
			problem.setDetail(e.getMessage());
			problem.setType(URI.create("https://api.lm.com/errors/" + e.getProblemCode().getCode()));
			problem.setTitle("Domain Error");
			problem.setProperty("code", e.getProblemCode().getCode());
			return ResponseEntity.status(HttpStatus.CONFLICT).headers(createProblemJsonHeaders()).body(problem);
		}

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			e.getProblemCode().getMessage()
		);

		// TODO :: 에러에 대한 문서로 연결 되면 좋음
		problemDetail.setType(URI.create("https://api.lm.com/errors/" + e.getProblemCode().getCode()));
		problemDetail.setTitle("Domain Error");
		problemDetail.setProperty("code", e.getProblemCode().getCode());

		return ResponseEntity.badRequest().headers(createProblemJsonHeaders()).body(problemDetail);
	}


	/**
	 * AuthenticationException 을 가로챈다.
	 * @param e AuthenticationException
	 * @return 401, ResponseEntity
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException e) {

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.UNAUTHORIZED,
			e.getProblemCode().getMessage()
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/" + e.getProblemCode().getCode()));
		problemDetail.setTitle("Authentication Error");
		problemDetail.setProperty("code", e.getProblemCode().getCode());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(createProblemJsonHeaders()).body(problemDetail);
	}

	/**
	 * AuthorizationException 을 가로챈다.
	 * @param e AuthorizationException
	 * @return 403, ResponseEntity
	 */
	@ExceptionHandler(AuthorizationException.class)
	public ResponseEntity<ProblemDetail> handleAuthorizationException(AuthorizationException e) {

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.FORBIDDEN,
			e.getProblemCode().getMessage()
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/" + e.getProblemCode().getCode()));
		problemDetail.setTitle("Authorization Error");
		problemDetail.setProperty("code", e.getProblemCode().getCode());

		return ResponseEntity.status(HttpStatus.FORBIDDEN).headers(createProblemJsonHeaders()).body(problemDetail);
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

		return ResponseEntity.badRequest().headers(createProblemJsonHeaders()).body(problemDetail);
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

		return ResponseEntity.badRequest().headers(createProblemJsonHeaders()).body(problemDetail);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ProblemDetail> handleBadRequest(HttpMessageNotReadableException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

		problemDetail.setType(URI.create("https://api.lm.com/errors/invalid-json-request"));
		problemDetail.setTitle("Bad Request");
		problemDetail.setProperty("code", "INVALID JSON");

		return ResponseEntity.badRequest().headers(createProblemJsonHeaders()).body(problemDetail);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException e) {
		// 인증 컨텍스트 관련 IllegalStateException은 인증 오류로 처리
		if (e.getMessage() != null && e.getMessage().contains("인증된 사용자의 컨텍스트를 찾을 수 없습니다")) {
			ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNAUTHORIZED,
				"[System] 인증 컨텍스트를 찾을 수 없습니다."
			);

			problemDetail.setType(URI.create("https://api.lm.com/errors/AUTH007"));
			problemDetail.setTitle("Authentication Context Not Found");
			problemDetail.setProperty("code", "AUTH007");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.headers(createProblemJsonHeaders())
				.body(problemDetail);
		}

		// 기타 IllegalStateException은 내부 서버 오류로 처리
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"[System] 내부 서버 오류가 발생했습니다."
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/internal-error"));
		problemDetail.setTitle("Internal Server Error");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.headers(createProblemJsonHeaders())
			.body(problemDetail);
	}

	/**
	 * 필수 요청 파라미터가 누락된 경우를 처리한다.
	 * @param e MissingServletRequestParameterException
	 * @return 400, ResponseEntity
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ProblemDetail> handleMissingServletRequestParameter(
		MissingServletRequestParameterException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			String.format("Required request parameter '%s' is missing", e.getParameterName())
		);

		problemDetail.setType(URI.create("https://api.lm.com/errors/missing-parameter"));
		problemDetail.setTitle("Missing Required Parameter");
		problemDetail.setProperty("code", "MISSING_PARAMETER");
		problemDetail.setProperty("parameterName", e.getParameterName());

		return ResponseEntity.badRequest().headers(createProblemJsonHeaders()).body(problemDetail);
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

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.headers(createProblemJsonHeaders())
			.body(problemDetail);
	}

	private HttpHeaders createProblemJsonHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/problem+json;charset=UTF-8"));
		return headers;
	}

}
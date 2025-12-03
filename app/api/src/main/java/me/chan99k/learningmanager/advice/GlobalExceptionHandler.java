package me.chan99k.learningmanager.advice;

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

import me.chan99k.learningmanager.auth.AuthProblemCode;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.exception.ProblemCode;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.session.SessionProblemCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * DomainException 을 가로챈다.
	 * @param e DomainException
	 * @return HTTP 상태 코드는 ProblemCode에 따라 결정됨
	 */
	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ProblemDetail> handleDomainException(DomainException e) {
		ProblemCode problemCode = e.getProblemCode();

		// 인증 관련 예외 - 401 UNAUTHORIZED
		if (isAuthenticationError(problemCode)) {
			return createErrorResponse(HttpStatus.UNAUTHORIZED, "Domain Error", e);
		}

		// 토큰 없음 예외 - 404 NOT_FOUND
		if (problemCode == AuthProblemCode.TOKEN_NOT_FOUND) {
			return createErrorResponse(HttpStatus.NOT_FOUND, "Domain Error", e);
		}

		// 접근 제어 관련 예외 - 403 FORBIDDEN
		if (isAccessControlError(problemCode)) {
			return createErrorResponse(HttpStatus.FORBIDDEN, "Authorization Error", e);
		}

		// 계정 없음 예외 - 404 NOT_FOUND
		if (problemCode == MemberProblemCode.ACCOUNT_NOT_FOUND) {
			ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
			problem.setDetail(e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(createProblemJsonHeaders()).body(problem);
		}

		// 이메일 중복 예외 - 409 CONFLICT
		if (problemCode == MemberProblemCode.EMAIL_ALREADY_EXISTS) {
			return createErrorResponse(HttpStatus.CONFLICT, "Domain Error", e);
		}

		// 기타 도메인 예외 - 400 BAD_REQUEST
		return createErrorResponse(HttpStatus.BAD_REQUEST, "Domain Error", e);
	}

	/**
	 * 인증 관련 에러인지 확인한다.
	 */
	private boolean isAuthenticationError(ProblemCode problemCode) {
		return problemCode == AuthProblemCode.INVALID_CREDENTIALS
			|| problemCode == AuthProblemCode.INVALID_TOKEN
			|| problemCode == AuthProblemCode.EXPIRED_TOKEN
			|| problemCode == AuthProblemCode.REVOKED_TOKEN;
	}

	/**
	 * 접근 제어 관련 에러인지 확인한다.
	 */
	private boolean isAccessControlError(ProblemCode problemCode) {
		return problemCode == CourseProblemCode.NOT_COURSE_MANAGER
			|| problemCode == CourseProblemCode.ADMIN_ONLY_COURSE_CREATION
			|| problemCode == SessionProblemCode.NOT_SESSION_HOST
			|| problemCode == SessionProblemCode.NOT_SESSION_PARTICIPANT
			|| problemCode == MemberProblemCode.ADMIN_ONLY_ACTION;
	}

	/**
	 * 표준화된 에러 응답을 생성한다.
	 */
	private ResponseEntity<ProblemDetail> createErrorResponse(HttpStatus status, String title, DomainException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, e.getProblemCode().getMessage());
		problemDetail.setType(URI.create("https://api.lm.com/errors/" + e.getProblemCode().getCode()));
		problemDetail.setTitle(title);
		problemDetail.setProperty("code", e.getProblemCode().getCode());
		return ResponseEntity.status(status).headers(createProblemJsonHeaders()).body(problemDetail);
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
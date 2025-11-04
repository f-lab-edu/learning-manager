package me.chan99k.learningmanager.adapter.web.attendance;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.attendance.provides.AttendanceCheckOut;
import me.chan99k.learningmanager.application.attendance.requires.QRCodeGenerator;
import me.chan99k.learningmanager.common.exception.AuthenticationException;

@WebMvcTest(controllers = AttendanceCheckOutController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import({
	GlobalExceptionHandler.class,
})
class AttendanceCheckOutControllerTest {

	private static final Long SESSION_ID = 1L;
	private static final Long MEMBER_ID = 100L;
	private static final String VALID_QR_TOKEN = "SESSION_1_1704110400000";
	private static final String INVALID_QR_TOKEN = "INVALID_TOKEN";
	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@MockBean
	AttendanceCheckOut attendanceCheckOutService;
	@MockBean
	QRCodeGenerator qrCodeGenerator;
	@MockBean(name = "memberTaskExecutor")
	Executor memberTaskExecutor;
	@MockBean(name = "courseTaskExecutor")
	AsyncTaskExecutor courseTaskExecutor;
	@MockBean
	java.time.Clock clock;

	@MockBean
	UserContext userContext;

	@BeforeEach
	void setUp() {
		// Executor가 동기적으로 실행하도록 설정
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(memberTaskExecutor).execute(any(Runnable.class));

		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(courseTaskExecutor).execute(any(Runnable.class));

		// UserContext 모킹
		given(userContext.getCurrentMemberId()).willReturn(MEMBER_ID);
		given(userContext.isAuthenticated()).willReturn(true);
	}


	@Test
	@DisplayName("[Success] 유효한 QR 토큰으로 체크아웃에 성공한다")
	void checkOut_success() throws Exception {
		// Given
		AttendanceCheckOut.Request request = new AttendanceCheckOut.Request(SESSION_ID);
		AttendanceCheckOut.Response response = new AttendanceCheckOut.Response(
			"attendance-id",
			SESSION_ID,
			MEMBER_ID,
			Instant.parse("2024-01-01T15:00:00Z"),
			"PRESENT"
		);

		given(qrCodeGenerator.validateQrCode(VALID_QR_TOKEN, SESSION_ID)).willReturn(true);
		given(attendanceCheckOutService.checkOut(any(AttendanceCheckOut.Request.class)))
			.willReturn(response);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-out/{token}", VALID_QR_TOKEN)
				.header("Authorization", "Bearer valid-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.attendanceId").value("attendance-id"))
			.andExpect(jsonPath("$.sessionId").value(SESSION_ID))
			.andExpect(jsonPath("$.memberId").value(MEMBER_ID))
			.andExpect(jsonPath("$.status").value("PRESENT"));

		verify(qrCodeGenerator).validateQrCode(VALID_QR_TOKEN, SESSION_ID);
		verify(attendanceCheckOutService).checkOut(any(AttendanceCheckOut.Request.class));
	}

	@Test
	@DisplayName("[Failure] 잘못된 QR 토큰으로 체크아웃 시도 시 401 Unauthorized를 반환한다")
	void checkOut_failure_invalid_qr_token() throws Exception {
		// Given
		AttendanceCheckOut.Request request = new AttendanceCheckOut.Request(SESSION_ID);

		given(qrCodeGenerator.validateQrCode(INVALID_QR_TOKEN, SESSION_ID)).willReturn(false);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-out/{token}", INVALID_QR_TOKEN)
				.header("Authorization", "Bearer valid-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));

		verify(qrCodeGenerator).validateQrCode(INVALID_QR_TOKEN, SESSION_ID);
		verify(attendanceCheckOutService, never()).checkOut(any(AttendanceCheckOut.Request.class));
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자가 체크아웃 시도 시 401 Unauthorized를 반환한다")
	void checkOut_failure_unauthenticated() throws Exception {
		// Given
		AttendanceCheckOut.Request request = new AttendanceCheckOut.Request(SESSION_ID);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-out/{token}", VALID_QR_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("[Failure] 잘못된 JSON 형식인 경우 400 Bad Request를 반환한다")
	void checkOut_failure_invalid_json() throws Exception {
		// Given
		String invalidJson = "{\"sessionId\":}";

		given(qrCodeGenerator.validateQrCode(VALID_QR_TOKEN, null)).willReturn(true);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-out/{token}", VALID_QR_TOKEN)
				.header("Authorization", "Bearer valid-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 어플리케이션 서비스에서 인증 예외 발생 시 401 Unauthorized를 반환한다")
	void checkOut_failure_service_authentication_error() throws Exception {
		// Given
		AttendanceCheckOut.Request request = new AttendanceCheckOut.Request(SESSION_ID);

		given(qrCodeGenerator.validateQrCode(VALID_QR_TOKEN, SESSION_ID)).willReturn(true);
		given(attendanceCheckOutService.checkOut(any(AttendanceCheckOut.Request.class)))
			.willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-out/{token}", VALID_QR_TOKEN)
				.header("Authorization", "Bearer valid-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));

		verify(qrCodeGenerator).validateQrCode(VALID_QR_TOKEN, SESSION_ID);
		verify(attendanceCheckOutService).checkOut(any(AttendanceCheckOut.Request.class));
	}
}
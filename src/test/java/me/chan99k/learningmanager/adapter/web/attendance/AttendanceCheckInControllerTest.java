package me.chan99k.learningmanager.adapter.web.attendance;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.AfterEach;
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

import me.chan99k.learningmanager.adapter.auth.AccessTokenProvider;
import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.adapter.auth.BcryptPasswordEncoder;
import me.chan99k.learningmanager.adapter.auth.JwtCredentialProvider;
import me.chan99k.learningmanager.adapter.auth.jwt.AccessJwtTokenProvider;
import me.chan99k.learningmanager.adapter.auth.jwt.InMemoryJwtTokenRevocationProvider;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.attendance.provides.AttendanceCheckIn;
import me.chan99k.learningmanager.application.attendance.requires.QRCodeGenerator;
import me.chan99k.learningmanager.common.exception.AuthenticationException;

@WebMvcTest(controllers = AttendanceCheckInController.class)
@Import({
	GlobalExceptionHandler.class,
	JwtCredentialProvider.class,
	AccessJwtTokenProvider.class,
	InMemoryJwtTokenRevocationProvider.class,
	BcryptPasswordEncoder.class
})
class AttendanceCheckInControllerTest {

	private static final Long SESSION_ID = 1L;
	private static final Long MEMBER_ID = 100L;
	private static final String VALID_QR_TOKEN = "SESSION_1_1704110400000";
	private static final String INVALID_QR_TOKEN = "INVALID_TOKEN";
	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@MockBean
	AttendanceCheckIn attendanceCheckInService;
	@MockBean
	QRCodeGenerator qrCodeGenerator;
	@MockBean
	AccessTokenProvider<Long> accessTokenProvider;
	@MockBean(name = "memberTaskExecutor")
	Executor memberTaskExecutor;
	@MockBean(name = "courseTaskExecutor")
	AsyncTaskExecutor courseTaskExecutor;
	@MockBean
	java.time.Clock clock;

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

		// 인증 컨텍스트 설정
		AuthenticationContextHolder.setCurrentMemberId(MEMBER_ID);

		// AccessTokenProvider 모킹
		given(accessTokenProvider.validateAccessToken("valid-access-token")).willReturn(true);
		given(accessTokenProvider.getIdFromAccessToken("valid-access-token")).willReturn(MEMBER_ID);
	}

	@AfterEach
	void tearDown() {
		AuthenticationContextHolder.clear();
	}

	@Test
	@DisplayName("[Success] 유효한 QR 토큰으로 체크인에 성공한다")
	void checkIn_success() throws Exception {
		// Given
		AttendanceCheckIn.Request request = new AttendanceCheckIn.Request(SESSION_ID);
		AttendanceCheckIn.Response response = new AttendanceCheckIn.Response(
			"attendance-id",
			SESSION_ID,
			MEMBER_ID,
			Instant.parse("2024-01-01T10:00:00Z"),
			"PRESENT"
		);

		given(qrCodeGenerator.validateQrCode(VALID_QR_TOKEN, SESSION_ID)).willReturn(true);
		given(attendanceCheckInService.checkIn(any(AttendanceCheckIn.Request.class)))
			.willReturn(response);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-in/{token}", VALID_QR_TOKEN)
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
		verify(attendanceCheckInService).checkIn(any(AttendanceCheckIn.Request.class));
	}

	@Test
	@DisplayName("[Failure] 잘못된 QR 토큰으로 체크인 시도 시 401 Unauthorized를 반환한다")
	void checkIn_failure_invalid_qr_token() throws Exception {
		// Given
		AttendanceCheckIn.Request request = new AttendanceCheckIn.Request(SESSION_ID);

		given(qrCodeGenerator.validateQrCode(INVALID_QR_TOKEN, SESSION_ID)).willReturn(false);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-in/{token}", INVALID_QR_TOKEN)
				.header("Authorization", "Bearer valid-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));

		verify(qrCodeGenerator).validateQrCode(INVALID_QR_TOKEN, SESSION_ID);
		verify(attendanceCheckInService, never()).checkIn(any(AttendanceCheckIn.Request.class));
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자가 체크인 시도 시 401 Unauthorized를 반환한다")
	void checkIn_failure_unauthenticated() throws Exception {
		// Given
		AttendanceCheckIn.Request request = new AttendanceCheckIn.Request(SESSION_ID);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-in/{token}", VALID_QR_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));
	}

	@Test
	@DisplayName("[Failure] 잘못된 JSON 형식인 경우 400 Bad Request를 반환한다")
	void checkIn_failure_invalid_json() throws Exception {
		// Given
		String invalidJson = "{\"sessionId\":}";

		given(qrCodeGenerator.validateQrCode(VALID_QR_TOKEN, null)).willReturn(true);

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-in/{token}", VALID_QR_TOKEN)
				.header("Authorization", "Bearer valid-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 어플리케이션 서비스에서 인증 예외 발생 시 401 Unauthorized를 반환한다")
	void checkIn_failure_service_authentication_error() throws Exception {
		// Given
		AttendanceCheckIn.Request request = new AttendanceCheckIn.Request(SESSION_ID);

		given(qrCodeGenerator.validateQrCode(VALID_QR_TOKEN, SESSION_ID)).willReturn(true);
		given(attendanceCheckInService.checkIn(any(AttendanceCheckIn.Request.class)))
			.willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		// When & Then
		mockMvc.perform(post("/api/v1/attendance/check-in/{token}", VALID_QR_TOKEN)
				.header("Authorization", "Bearer valid-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"));

		verify(qrCodeGenerator).validateQrCode(VALID_QR_TOKEN, SESSION_ID);
		verify(attendanceCheckInService).checkIn(any(AttendanceCheckIn.Request.class));
	}
}
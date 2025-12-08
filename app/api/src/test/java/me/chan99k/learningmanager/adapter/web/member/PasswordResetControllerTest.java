package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.controller.member.PasswordResetController;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.PasswordResetConfirmation;
import me.chan99k.learningmanager.member.PasswordResetRequest;
import me.chan99k.learningmanager.member.PasswordResetVerification;

@WebMvcTest(value = PasswordResetController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@DisplayName("PasswordResetController 테스트")
class PasswordResetControllerTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String RESET_TOKEN = "reset-token-abc123";
	private static final String NEW_PASSWORD = "NewPassword123!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PasswordResetRequest passwordResetRequest;

	@MockBean
	private PasswordResetVerification passwordResetVerification;

	@MockBean
	private PasswordResetConfirmation passwordResetConfirmation;

	@MockBean
	private JwtProvider jwtProvider;

	@Nested
	@DisplayName("비밀번호 재설정 요청 API 테스트 (POST /api/v1/auth/password/reset-request)")
	class RequestPasswordResetTest {

		@Test
		@DisplayName("[Success] 가입된 이메일로 비밀번호 재설정 요청 성공 - 200 OK")
		void request_password_reset_test_01() throws Exception {
			PasswordResetRequest.Request request = new PasswordResetRequest.Request(TEST_EMAIL);
			doNothing().when(passwordResetRequest).requestReset(any(PasswordResetRequest.Request.class));

			mockMvc.perform(post("/api/v1/auth/password/reset-request")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("[Failure] 이메일 누락 - 400 Bad Request")
		void request_password_reset_test_02() throws Exception {
			String requestJson = "{}";

			mockMvc.perform(post("/api/v1/auth/password/reset-request")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 빈 이메일 - 400 Bad Request")
		void request_password_reset_test_03() throws Exception {
			PasswordResetRequest.Request request = new PasswordResetRequest.Request("");

			mockMvc.perform(post("/api/v1/auth/password/reset-request")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 잘못된 이메일 형식 - 400 Bad Request")
		void request_password_reset_test_04() throws Exception {
			PasswordResetRequest.Request request = new PasswordResetRequest.Request("invalid-email");

			mockMvc.perform(post("/api/v1/auth/password/reset-request")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 가입되지 않은 이메일 - 400 Bad Request")
		void request_password_reset_test_05() throws Exception {
			PasswordResetRequest.Request request = new PasswordResetRequest.Request("notfound@example.com");
			doThrow(new DomainException(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND))
				.when(passwordResetRequest).requestReset(any(PasswordResetRequest.Request.class));

			mockMvc.perform(post("/api/v1/auth/password/reset-request")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("DML030"));
		}
	}

	@Nested
	@DisplayName("토큰 검증 API 테스트 (GET /api/v1/auth/password/verify-token)")
	class VerifyResetTokenTest {

		@Test
		@DisplayName("[Success] 유효한 토큰 검증 성공 - 200 OK")
		void verify_reset_token_test_01() throws Exception {
			PasswordResetVerification.Response response = new PasswordResetVerification.Response(true, TEST_EMAIL);
			given(passwordResetVerification.verifyResetToken(any(PasswordResetVerification.Request.class)))
				.willReturn(response);

			mockMvc.perform(get("/api/v1/auth/password/verify-token")
					.param("token", RESET_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.tokenValid").value(true))
				.andExpect(jsonPath("$.email").value(TEST_EMAIL));
		}

		@Test
		@DisplayName("[Success] 유효하지 않은 토큰 - 200 OK with tokenValid=false")
		void verify_reset_token_test_02() throws Exception {
			PasswordResetVerification.Response response = new PasswordResetVerification.Response(false, null);
			given(passwordResetVerification.verifyResetToken(any(PasswordResetVerification.Request.class)))
				.willReturn(response);

			mockMvc.perform(get("/api/v1/auth/password/verify-token")
					.param("token", "invalid-token"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.tokenValid").value(false))
				.andExpect(jsonPath("$.email").doesNotExist());
		}

		@Test
		@DisplayName("[Failure] 토큰 파라미터 누락 - 400 Bad Request")
		void verify_reset_token_test_03() throws Exception {
			mockMvc.perform(get("/api/v1/auth/password/verify-token"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Missing Required Parameter"))
				.andExpect(jsonPath("$.parameterName").value("token"));
		}
	}

	@Nested
	@DisplayName("비밀번호 재설정 확정 API 테스트 (POST /api/v1/auth/password/reset)")
	class ConfirmPasswordResetTest {

		@Test
		@DisplayName("[Success] 비밀번호 재설정 성공 - 200 OK")
		void confirm_password_reset_test_01() throws Exception {
			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request(RESET_TOKEN,
				NEW_PASSWORD);
			doNothing().when(passwordResetConfirmation).confirmReset(any(PasswordResetConfirmation.Request.class));

			mockMvc.perform(post("/api/v1/auth/password/reset")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("[Failure] 토큰 누락 - 400 Bad Request")
		void confirm_password_reset_test_02() throws Exception {
			String requestJson = "{\"newPassword\": \"" + NEW_PASSWORD + "\"}";

			mockMvc.perform(post("/api/v1/auth/password/reset")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 새 비밀번호 누락 - 400 Bad Request")
		void confirm_password_reset_test_03() throws Exception {
			String requestJson = "{\"token\": \"" + RESET_TOKEN + "\"}";

			mockMvc.perform(post("/api/v1/auth/password/reset")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 유효하지 않은 토큰 - 400 Bad Request")
		void confirm_password_reset_test_04() throws Exception {
			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request("invalid-token",
				NEW_PASSWORD);
			doThrow(new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN))
				.when(passwordResetConfirmation).confirmReset(any(PasswordResetConfirmation.Request.class));

			mockMvc.perform(post("/api/v1/auth/password/reset")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("DML031"));
		}

		@Test
		@DisplayName("[Failure] 회원을 찾을 수 없음 - 400 Bad Request")
		void confirm_password_reset_test_05() throws Exception {
			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request(RESET_TOKEN,
				NEW_PASSWORD);
			doThrow(new DomainException(MemberProblemCode.MEMBER_NOT_FOUND))
				.when(passwordResetConfirmation).confirmReset(any(PasswordResetConfirmation.Request.class));

			mockMvc.perform(post("/api/v1/auth/password/reset")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"));
		}

		@Test
		@DisplayName("[Failure] 서버 오류 - 500 Internal Server Error")
		void confirm_password_reset_test_06() throws Exception {
			PasswordResetConfirmation.Request request = new PasswordResetConfirmation.Request(RESET_TOKEN,
				NEW_PASSWORD);
			doThrow(new RuntimeException("데이터베이스 연결 오류"))
				.when(passwordResetConfirmation).confirmReset(any(PasswordResetConfirmation.Request.class));

			mockMvc.perform(post("/api/v1/auth/password/reset")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Internal Server Error"))
				.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
		}
	}
}
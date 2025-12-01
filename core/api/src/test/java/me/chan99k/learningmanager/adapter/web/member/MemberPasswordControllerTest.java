package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@WebMvcTest(controllers = MemberPasswordController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
class MemberPasswordControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private me.chan99k.learningmanager.application.auth.requires.UserContext userContext;

	@MockBean
	private AccountPasswordChange passwordChangeService;

	@MockBean
	private AccountPasswordReset passwordResetService;

	@MockBean
	private Executor memberTaskExecutor;

	@BeforeEach
	void setupExecutor() {
		// CompletableFuture.supplyAsync()가 즉시 실행되도록 설정
		// Executor.execute()를 모킹하여 바로 실행
		doAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).when(memberTaskExecutor).execute(any(Runnable.class));
	}

	@Nested
	@DisplayName("비밀번호 변경 테스트")
	class PasswordChangeTests {

		@Test
		@DisplayName("[Success] 유효한 요청으로 비밀번호 변경에 성공한다.")
		void test01() throws Exception {
			var request = new AccountPasswordChange.Request("test@example.com", "NewSecurePass456@");

			given(userContext.getCurrentMemberId()).willReturn(1L);
			given(userContext.isAuthenticated()).willReturn(true);

			given(passwordChangeService.changePassword(any(AccountPasswordChange.Request.class)))
				.willReturn(new AccountPasswordChange.Response());

			mockMvc.perform(put("/api/v1/members/change-password")
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer mock-token")
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("비밀번호 재설정 테스트")
	class PasswordResetTests {

		@Test
		@DisplayName("[Success] 비밀번호 재설정 요청 성공 - 200 응답")
		void resetPassword_Success() throws Exception {
			AccountPasswordReset.RequestResetRequest request =
				new AccountPasswordReset.RequestResetRequest("test@example.com");

			AccountPasswordReset.RequestResetResponse response =
				new AccountPasswordReset.RequestResetResponse("가입시 사용한 이메일: test@example.com 로 비밀번호 재설정 메일을 발송했습니다.");

			given(passwordResetService.requestReset(any(AccountPasswordReset.RequestResetRequest.class)))
				.willReturn(response);

			mockMvc.perform(post("/api/v1/members/reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("[Failure] 비밀번호 재설정 요청 실패 - 가입되지 않은 이메일로 409 응답을 반환하는 케이스")
		void resetPassword_Failure_EmailNotFound() throws Exception {
			AccountPasswordReset.RequestResetRequest request =
				new AccountPasswordReset.RequestResetRequest("notfound@example.com");

			given(passwordResetService.requestReset(any(AccountPasswordReset.RequestResetRequest.class)))
				.willThrow(new DomainException(MemberProblemCode.PASSWORD_RESET_EMAIL_NOT_FOUND));

			mockMvc.perform(post("/api/v1/members/reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

		}

		@Test
		@DisplayName("[Success] 사용자가 전달받은 토큰으로 검증에 성공하고 JSON 응답을 반환한다")
		void verifyResetToken_Success() throws Exception {
			String token = "valid-token-123";
			String email = "test@example.com";

			AccountPasswordReset.TokenVerificationResponse response =
				new AccountPasswordReset.TokenVerificationResponse(
					true,
					email,
					token,
					"토큰이 유효합니다. 새 비밀번호를 설정하세요."
				);

			given(passwordResetService.verifyResetToken(token)).willReturn(response);

			mockMvc.perform(get("/api/v1/members/reset-password")
					.param("token", token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.tokenValid").value(true))
				.andExpect(jsonPath("$.userEmail").value(email))
				.andExpect(jsonPath("$.token").value(token))
				.andExpect(jsonPath("$.message").value("토큰이 유효합니다. 새 비밀번호를 설정하세요."));
		}

		@Test
		@DisplayName("[Failure] 토큰 검증에 실패하면 400 Bad Request와 JSON 에러 응답을 반환한다")
		void verifyResetToken_Failure_InvalidToken() throws Exception {
			String invalidToken = "invalid-token";

			given(passwordResetService.verifyResetToken(invalidToken))
				.willThrow(new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN));

			mockMvc.perform(get("/api/v1/members/reset-password")
					.param("token", invalidToken))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.tokenValid").value(false))
				.andExpect(jsonPath("$.userEmail").isEmpty())
				.andExpect(jsonPath("$.token").value(invalidToken));
		}

		@Test
		@DisplayName("[Success] 비밀번호 재설정 확인 성공")
		void confirmReset_Success() throws Exception {
			AccountPasswordReset.ConfirmResetRequest request =
				new AccountPasswordReset.ConfirmResetRequest("valid-token-123", "NewSecurePass123!");

			given(passwordResetService.confirmReset(any(AccountPasswordReset.ConfirmResetRequest.class)))
				.willReturn(new AccountPasswordReset.ConfirmResetResponse());

			mockMvc.perform(post("/api/v1/members/confirm-reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNoContent()); // 204 No Content
		}

		@Test
		@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 요청에 토큰 없음")
		void confirmReset_Failure_NoToken() throws Exception {
			// token is null
			AccountPasswordReset.ConfirmResetRequest request =
				new AccountPasswordReset.ConfirmResetRequest(null, "NewSecurePass123!");

			mockMvc.perform(post("/api/v1/members/confirm-reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 서비스 에러")
		void confirmReset_Failure_ServiceError() throws Exception {
			AccountPasswordReset.ConfirmResetRequest request =
				new AccountPasswordReset.ConfirmResetRequest("valid-token-123", "NewSecurePass123!");

			given(passwordResetService.confirmReset(any(AccountPasswordReset.ConfirmResetRequest.class)))
				.willThrow(new DomainException(MemberProblemCode.NEW_PASSWORD_SAME_AS_CURRENT));

			mockMvc.perform(post("/api/v1/members/confirm-reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Failure] 잘못된 요청 형식 - 400 응답")
		void resetPassword_BadRequest() throws Exception {
			String invalidRequest = "{ \"invalidField\": \"value\" }";

			mockMvc.perform(post("/api/v1/members/reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidRequest))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Failure] 빈 요청 본문에 400 응답")
		void confirmReset_EmptyBody() throws Exception {
			mockMvc.perform(post("/api/v1/members/confirm-reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"))
				.andExpect(status().isBadRequest());
		}
	}

}
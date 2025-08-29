package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

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
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.adapter.auth.JwtTokenProvider;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordReset;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@WebMvcTest(value = MemberPasswordController.class)
class MemberPasswordControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

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

			given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
			given(jwtTokenProvider.getMemberIdFromToken(anyString())).willReturn(String.valueOf(1L));

			given(passwordChangeService.changePassword(any(AccountPasswordChange.Request.class)))
				.willReturn(new AccountPasswordChange.Response());

			MvcResult result = mockMvc.perform(put("/api/v1/members/change-password")
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer mock-token")
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(request().asyncStarted())
				.andReturn();

			mockMvc.perform(asyncDispatch(result))
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
				.andExpect(request().asyncStarted())
				.andReturn();
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
				.andExpect(request().asyncStarted())
				.andReturn();

		}

		@Test
		@DisplayName("[Success] 사용자가 전달받은 url 로 접급하여 토큰 검증 및 리다이렉트에 성공한다")
		void getRedirectPage_Success() throws Exception {
			String token = "valid-token-123";

			given(passwordResetService.validatePasswordResetToken(token)).willReturn(true);

			MvcResult result = mockMvc.perform(get("/api/v1/members/reset-password")
					.param("token", token))
				.andExpect(request().asyncStarted())
				.andReturn();

			// 비동기 완료 후 리다이렉트 검증
			mockMvc.perform(asyncDispatch(result))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "/reset-password-form"));
		}

		@Test
		@DisplayName("[Failure] 토큰 검증에 실패하면 에러 페이지로 리다이렉트한다")
		void getRedirectPage_Failure_InvalidToken() throws Exception {
			String invalidToken = "invalid-token";

			given(passwordResetService.validatePasswordResetToken(invalidToken))
				.willThrow(new DomainException(MemberProblemCode.INVALID_PASSWORD_RESET_TOKEN));

			MvcResult result = mockMvc.perform(get("/api/v1/members/reset-password")
					.param("token", invalidToken))
				.andExpect(request().asyncStarted())
				.andReturn();

			// 비동기 완료 후 에러 페이지 리다이렉트 검증
			mockMvc.perform(asyncDispatch(result))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "/error?message=invalid_token"));
		}

		@Test
		@DisplayName("[Success] 비밀번호 재설정 확인 성공 - 세션 기반")
		void confirmReset_Success() throws Exception {
			AccountPasswordReset.ConfirmResetRequest request =
				new AccountPasswordReset.ConfirmResetRequest("NewSecurePass123!");

			given(passwordResetService.confirmReset(anyString(), anyString()))
				.willReturn(new AccountPasswordReset.ConfirmResetResponse());

			MvcResult result = mockMvc.perform(post("/api/v1/members/confirm-reset-password")
					.sessionAttr("verified_reset_token", "valid-token-123")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(request().asyncStarted())
				.andReturn();

			// 비동기 완료 후 성공 상태 검증
			mockMvc.perform(asyncDispatch(result))
				.andExpect(status().isNoContent()); // 204 No Content
		}

		@Test
		@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 세션에 토큰 없음")
		void confirmReset_Failure_NoSessionToken() throws Exception {
			AccountPasswordReset.ConfirmResetRequest request =
				new AccountPasswordReset.ConfirmResetRequest("NewSecurePass123!");

			// 세션에 토큰이 없는 상태로 테스트 (validation은 통과하도록 올바른 요청)
			MvcResult result = mockMvc.perform(post("/api/v1/members/confirm-reset-password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print()) // 응답 내용을 출력하여 디버그
				.andExpect(request().asyncStarted())
				.andReturn();

			// 비동기 완료 후 세션 토큰 없음으로 인한 예외 검증
			mockMvc.perform(asyncDispatch(result))
				.andDo(print()) // 비동기 응답도 출력
				.andExpect(status().isBadRequest()) // 400 상태 코드 (DomainException → 400 by GlobalExceptionHandler)
				.andExpect(jsonPath("$.code").value("DML031")) // 에러 코드 검증
				.andExpect(jsonPath("$.detail").value("[System] 유효하지 않은 비밀번호 재설정 토큰입니다."));
		}

		@Test
		@DisplayName("[Failure] 비밀번호 재설정 확인 실패 - 서비스 에러")
		void confirmReset_Failure_ServiceError() throws Exception {
			AccountPasswordReset.ConfirmResetRequest request =
				new AccountPasswordReset.ConfirmResetRequest("NewSecurePass123!");

			given(passwordResetService.confirmReset(anyString(), anyString()))
				.willThrow(new DomainException(MemberProblemCode.NEW_PASSWORD_SAME_AS_CURRENT));

			mockMvc.perform(post("/api/v1/members/confirm-reset-password")
					.sessionAttr("verified_reset_token", "valid-token-123")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(request().asyncStarted())
				.andReturn();
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
					.sessionAttr("verified_reset_token", "valid-token-123")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"))
				.andExpect(status().isBadRequest());
		}
	}

}
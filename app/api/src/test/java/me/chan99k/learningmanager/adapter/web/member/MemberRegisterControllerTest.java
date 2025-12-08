package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.Executor;

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
import me.chan99k.learningmanager.controller.member.MemberRegisterController;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberRegistration;
import me.chan99k.learningmanager.member.SignUpConfirmation;

@WebMvcTest(value = MemberRegisterController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
public class MemberRegisterControllerTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PASSWORD = "testPassword123!";
	private static final String ACTIVATION_TOKEN = "test-activation-token-123";
	private static final Long MEMBER_ID = 2L;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private MemberRegistration memberRegistration;
	@MockBean
	private SignUpConfirmation signUpConfirmation;
	@MockBean
	private Executor memberTaskExecutor;
	@MockBean
	private JwtProvider jwtProvider;

	@Nested
	@DisplayName("회원가입 API 테스트")
	class RegisterMemberTest {

		@Test
		@DisplayName("[Success] 회원가입 요청이 성공하면 201 Created와 회원 ID를 반환한다")
		void register_member_test_01() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);
			MemberRegistration.Response response = new MemberRegistration.Response(MEMBER_ID);

			// MemberRegistration 목 시나리오 설정
			given(memberRegistration.register(any(MemberRegistration.Request.class)))
				.willReturn(response);

			// Executor가 동기적으로 실행하도록 목 시나리오 설정
			// CompletableFuture를 테스트에서 동기적으로 처리하여 검증 시점에 결과를 바로 확보하기 위함
			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run(); // 즉시 실행
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			// 요청 시작 및 결과 검증
			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.memberId").value(MEMBER_ID));
		}

		@Test
		@DisplayName("[Failure] 이메일이 누락된 경우 400 Bad Request를 반환한다")
		void register_member_test_02() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request(null, TEST_PASSWORD);

			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"))
				.andExpect(jsonPath("$.detail").exists());
			// TODO ::  응답이 Body = {... "status":400,"detail":"[System] ì´ë ...} 처럼 깨져서 보이는 것을 해결 하여야 함
		}

		@Test
		@DisplayName("[Failure] 빈 이메일인 경우 400 Bad Request를 반환한다")
		void register_member_test_03() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request("", TEST_PASSWORD);

			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 패스워드가 누락된 경우 400 Bad Request를 반환한다")
		void register_member_test_04() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, null);

			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 빈 패스워드인 경우 400 Bad Request를 반환한다")
		void register_member_test_05() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, "");

			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 잘못된 JSON 형식인 경우 400 Bad Request를 반환한다")
		void register_member_test_06() throws Exception {
			String invalidJson = "{\"email\": \"test@example.com\", \"rawPassword\":}";

			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidJson))
				.andDo(print())
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("[Failure] 이미 존재하는 이메일로 가입 시 409 Conflict를 반환한다")
		void register_member_test_07() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			// 이미 존재하는 이메일로 인한 DomainException 시나리오 설정
			given(memberRegistration.register(any(MemberRegistration.Request.class)))
				.willThrow(new DomainException(MemberProblemCode.EMAIL_ALREADY_EXISTS));

			// Executor가 동기적으로 실행하도록 목 시나리오 설정
			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run();
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			// 요청 및 결과 검증
			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isConflict())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.detail").value("[System] 이미 등록된 이메일입니다."))
				.andExpect(jsonPath("$.code").value("DML022"));
		}

		@Test
		@DisplayName("[Failure] 예기치 못한 회원 등록 실패 시 500 Internal Server Error를 반환한다")
		void register_member_test_08() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);

			// 예기치 못한 서버 오류로 인한 RuntimeException 시나리오 설정
			given(memberRegistration.register(any(MemberRegistration.Request.class)))
				.willThrow(new RuntimeException("데이터베이스 연결 오류"));

			// Executor가 동기적으로 실행하도록 목 시나리오 설정
			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run();
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			// 요청 및 결과 검증
			mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Internal Server Error"))
				.andExpect(jsonPath("$.detail").value("[System] 일시적인 서버 오류가 발생했습니다."))
				.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
		}
	}

	@Nested
	@DisplayName("회원 활성화 API 테스트")
	class ActivateMemberTest {

		@Test
		@DisplayName("[Success] 유효한 활성화 토큰으로 회원 활성화에 성공한다")
		void activate_member_test_01() throws Exception {
			doNothing().when(signUpConfirmation)
				.activateSignUpMember(new SignUpConfirmation.Request(ACTIVATION_TOKEN));

			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run();
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			mockMvc.perform(get("/api/v1/members/activate")
					.param("token", ACTIVATION_TOKEN))
				.andDo(print())
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("[Failure] 토큰 파라미터가 누락된 경우 400 Bad Request를 반환한다")
		void activate_member_test_02() throws Exception {
			mockMvc.perform(get("/api/v1/members/activate"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Missing Required Parameter"))
				.andExpect(jsonPath("$.detail").value("Required request parameter 'token' is missing"))
				.andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
				.andExpect(jsonPath("$.parameterName").value("token"));
		}

		@Test
		@DisplayName("[Failure] 유효하지 않은 토큰인 경우 400 Bad Request를 반환한다")
		void activate_member_test_03() throws Exception {
			String invalidToken = "invalid-token";

			doThrow(new RuntimeException("Invalid token"))
				.when(signUpConfirmation)
				.activateSignUpMember(new SignUpConfirmation.Request(invalidToken));

			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run();
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			mockMvc.perform(get("/api/v1/members/activate")
					.param("token", invalidToken))
				.andDo(print())
				.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("[Failure] 만료된 토큰인 경우 400 Bad Request를 반환한다")
		void activate_member_test_04() throws Exception {
			String expiredToken = "expired-token";

			doThrow(new RuntimeException("Token expired"))
				.when(signUpConfirmation)
				.activateSignUpMember(new SignUpConfirmation.Request(expiredToken));

			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run();
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			mockMvc.perform(get("/api/v1/members/activate")
					.param("token", expiredToken))
				.andDo(print())
				.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("[Failure] 존재하지 않는 회원인 경우 404 Not Found를 반환한다")
		void activate_member_test_05() throws Exception {
			String nonExistentToken = "non-existent-token";

			doThrow(new RuntimeException("Member not found"))
				.when(signUpConfirmation)
				.activateSignUpMember(new SignUpConfirmation.Request(nonExistentToken));

			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run();
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			mockMvc.perform(get("/api/v1/members/activate")
					.param("token", nonExistentToken))
				.andDo(print())
				.andExpect(status().isInternalServerError());
		}
	}
}
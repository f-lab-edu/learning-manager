package me.chan99k.learningmanager.adapter.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.Disabled;
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

import me.chan99k.learningmanager.adapter.web.member.MemberRegisterController;
import me.chan99k.learningmanager.application.member.MemberRegisterService;
import me.chan99k.learningmanager.application.member.provides.MemberRegistration;
import me.chan99k.learningmanager.application.member.provides.SignUpConfirmation;

@WebMvcTest(MemberRegisterController.class)
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
	private MemberRegisterService memberRegisterService;
	@MockBean
	private Executor memberTaskExecutor;

	@Nested
	@DisplayName("회원가입 API 테스트")
	class RegisterMemberTest {

		@Test
		@DisplayName("[Success] 회원가입 요청이 성공하면 201 Created와 회원 ID를 반환한다")
		void register_member_test_01() throws Exception {
			MemberRegistration.Request request = new MemberRegistration.Request(TEST_EMAIL, TEST_PASSWORD);
			MemberRegistration.Response response = new MemberRegistration.Response(MEMBER_ID);

			// MemberRegisterService 목 시나리오 설정
			given(memberRegisterService.register(any(MemberRegistration.Request.class)))
				.willReturn(response);

			// Executor가 동기적으로 실행하도록 목 시나리오 설정
			// CompletableFuture를 테스트에서 동기적으로 처리하여 검증 시점에 결과를 바로 확보하기 위함
			willAnswer(invocation -> {
				Runnable task = invocation.getArgument(0);
				task.run(); // 즉시 실행
				return null;
			}).given(memberTaskExecutor).execute(any(Runnable.class));

			// 요청 시작 및 결과 검증
			MvcResult mvcResult = mockMvc.perform(post("/api/v1/members/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(request().asyncStarted())
				.andReturn();

			// asyncDispatch() : Spring MVC가 관리하는 비동기 요청의 최종 결과를 MockMvc가 가져오는 단계, async 완료 신호를 처리
			// MockMvc 는 Servlet/DispatcherServlet 내부 상태를 기준으로 async 처리를 완료해야 응답을 읽을 수 있음
			mockMvc.perform(asyncDispatch(mvcResult))
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
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
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
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
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
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
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
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
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
		@Disabled
		@DisplayName("[Failure] 이미 존재하는 이메일로 가입 시 409 Conflict를 반환한다")
		void register_member_test_07() {
		}

		@Test
		@Disabled
		@DisplayName("[Failure] 예기치 못한 회원 등록 실패 시 500 Internal Server Error를 반환한다")
		void register_member_test_08() {
		}
	}

	@Nested
	@DisplayName("회원 활성화 API 테스트")
	class ActivateMemberTest {

		@Test
		@DisplayName("[Success] 유효한 활성화 토큰으로 회원 활성화에 성공한다")
		void activate_member_test_01() throws Exception {
			doNothing().when(memberRegisterService)
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
		@Disabled
		@DisplayName("[Failure] 토큰 파라미터가 누락된 경우 400 Bad Request를 반환한다")
		void activate_member_test_02() {
		}

		@Test
		@Disabled
		@DisplayName("[Failure] 유효하지 않은 토큰인 경우 400 Bad Request를 반환한다")
		void activate_member_test_03() {
		}

		@Test
		@Disabled
		@DisplayName("[Failure] 만료된 토큰인 경우 400 Bad Request를 반환한다")
		void activate_member_test_04() {
		}

		@Test
		@Disabled
		@DisplayName("[Failure] 존재하지 않는 회원인 경우 404 Not Found를 반환한다")
		void activate_member_test_05() {
		}
	}
}
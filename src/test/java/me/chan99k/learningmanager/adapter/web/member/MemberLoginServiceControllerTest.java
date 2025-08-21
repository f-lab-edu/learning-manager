package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import me.chan99k.learningmanager.application.member.provides.MemberLogin;

@WebMvcTest(MemberLoginController.class)
class MemberLoginServiceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private Executor memberTaskExecutor;
	@MockBean
	private MemberLogin memberLogin;

	@BeforeEach
	void setUp() {
		// 모든 테스트에서 Executor를 동기적으로 실행하도록 설정
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(memberTaskExecutor).execute(any(Runnable.class));

		// 기본적으로 성공 응답을 반환하도록 설정
		MemberLogin.Response mockResponse = new MemberLogin.Response("jwt_token_123");
		when(memberLogin.login(any(MemberLogin.Request.class))).thenReturn(mockResponse);
	}

	@Test
	@DisplayName("[Success] 올바른 요청으로 로그인 성공")
	void test01() throws Exception {
		String validRequest = """
			{
			        "email": "test@example.com",
			        "password": "password123"
			}
			""";

		MvcResult mvcResult = mockMvc.perform(post("/api/v1/members/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validRequest))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.accessToken").value("jwt_token_123"));
	}

	@Test
	@DisplayName("[Failure] 빈 이메일로 요청 시 400 에러")
	void test02() throws Exception {
		String invalidRequest = """
			{
			        "email": "",
			        "password": "password123"
			}
			""";

		// @NotBlank 검증은 비동기 처리 전에 발생
		mockMvc.perform(post("/api/v1/members/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 잘못된 이메일 형식으로 요청 시 400 에러")
	void test03() throws Exception {
		String invalidRequest = """
			{
			        "email": "invalid-email-format",
			        "password": "password123"
			}
			""";

		// @Email validation은 비동기 처리 전에 발생
		mockMvc.perform(post("/api/v1/members/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 빈 패스워드로 요청 시 400 에러")
	void test04() throws Exception {
		String invalidRequest = """
			{
			        "email": "test@example.com",
			        "password": ""
			}
			""";

		// @NotBlank 검증은 비동기 처리 전에 발생
		// 그 외 비밀번호 유효성 검증은 Password VO 에서 발생
		mockMvc.perform(post("/api/v1/members/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] JSON 형식이 잘못된 요청 시 400 에러")
	void test05() throws Exception {
		String invalidJsonRequest = "{ invalid json }";

		mockMvc.perform(post("/api/v1/members/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJsonRequest))
			.andExpect(status().isBadRequest());
	}

}


package me.chan99k.learningmanager.adapter.web.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.auth.requires.UserContext;
import me.chan99k.learningmanager.application.member.provides.MemberProfileRetrieval;
import me.chan99k.learningmanager.application.member.provides.MemberProfileUpdate;
import me.chan99k.learningmanager.application.member.provides.MemberWithdrawal;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthenticationException;
import me.chan99k.learningmanager.domain.exception.DomainException;

@WebMvcTest(controllers = MemberProfileController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class MemberProfileControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	MemberProfileUpdate memberProfileUpdate;

	@MockBean
	MemberProfileRetrieval memberProfileRetrieval;

	@MockBean
	MemberWithdrawal memberWithdrawal;

	@MockBean
	UserContext userContext;

	@MockBean(name = "memberTaskExecutor")
	Executor memberTaskExecutor;

	@BeforeEach
	void setupExecutor() {
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(memberTaskExecutor).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("공개 프로필 조회 성공")
	void test01() throws Exception {
		given(memberProfileRetrieval.getPublicProfile("nick"))
			.willReturn(new MemberProfileRetrieval.Response("img", "intro"));

		MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/{nickname}/profile-public", "nick"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.profileImageUrl").value("img"))
			.andExpect(jsonPath("$.selfIntroduction").value("intro"));

		// Mock 검증 강화
		verify(memberProfileRetrieval).getPublicProfile("nick");
	}

	@Test
	@DisplayName("공개 프로필 조회시 해당 회원이 없다면 400과 Problem code 반환")
	void test02() throws Exception {
		given(memberProfileRetrieval.getPublicProfile("ghost"))
			.willThrow(new DomainException(MEMBER_NOT_FOUND));

		MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/{nickname}/profile-public", "ghost"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value(MEMBER_NOT_FOUND.getMessage()));

		// Mock 검증 강화
		verify(memberProfileRetrieval).getPublicProfile("ghost");
	}

	@Test
	@DisplayName("내 프로필 조회시, 인증 성공하여 200을 반환")
	void test03() throws Exception {
		given(userContext.getCurrentMemberId()).willReturn(5L);
		given(userContext.isAuthenticated()).willReturn(true);
		given(memberProfileRetrieval.getProfile(5L))
			.willReturn(new MemberProfileRetrieval.Response("img", "intro"));

		MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/profile")
				.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1In0.test")
			)
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.profileImageUrl").value("img"));

		// Mock 검증 강화 - 정확한 memberId로 호출되었는지 확인
		verify(memberProfileRetrieval).getProfile(5L);
	}

	@Test
	@DisplayName("내 프로필 조회 - 미인증 시 401 반환")
	void test04() throws Exception {
		given(userContext.getCurrentMemberId())
			.willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		mockMvc.perform(get("/api/v1/members/profile"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("프로필 수정 - Authorization 헤더 없을 때 401")
	void test05() throws Exception {
		given(userContext.getCurrentMemberId())
			.willThrow(new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		mockMvc.perform(
				post("/api/v1/members/profile")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"profileImageUrl\":\"img\",\"selfIntroduction\":\"intro\"}")
			)
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("프로필 수정 - 인증 성공 200")
	void test06() throws Exception {
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1In0.validToken";
		given(userContext.getCurrentMemberId()).willReturn(5L);
		given(userContext.isAuthenticated()).willReturn(true);
		given(memberProfileUpdate.updateProfile(eq(5L), any(MemberProfileUpdate.Request.class)))
			.willReturn(new MemberProfileUpdate.Response(5L));

		mockMvc.perform(post("/api/v1/members/profile")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"profileImageUrl\":\"img\",\"selfIntroduction\":\"intro\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberId").value(5));

		verify(memberProfileUpdate).updateProfile(eq(5L), argThat(req ->
			"img".equals(req.profileImageUrl()) &&
				"intro".equals(req.selfIntroduction())
		));
	}

	@Test
	@DisplayName("[Success] 회원 탈퇴 요청이 성공하면 204 No Content를 반환한다")
	void withdrawal_Success() throws Exception {
		doNothing().when(memberWithdrawal).withdrawal();

		mockMvc.perform(delete("/api/v1/members/withdrawal"))
			.andExpect(status().isNoContent());

		verify(memberWithdrawal).withdrawal();
	}

}

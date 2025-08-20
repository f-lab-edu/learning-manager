package me.chan99k.learningmanager.adapter.web.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import me.chan99k.learningmanager.adapter.auth.JwtAuthenticationFilter;
import me.chan99k.learningmanager.adapter.auth.JwtTokenProvider;
import me.chan99k.learningmanager.application.member.provides.MemberProfileRetrieval;
import me.chan99k.learningmanager.application.member.provides.MemberProfileUpdate;
import me.chan99k.learningmanager.common.exception.DomainException;

@WebMvcTest(controllers = MemberProfileController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터, CORS 필터, 인코딩 필터 등 모든 서블릿 필터들을 테스트에서 제외
class MemberProfileControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	MemberProfileUpdate memberProfileUpdate;
	@MockBean
	MemberProfileRetrieval memberProfileRetrieval;
	@MockBean
	JwtTokenProvider jwtTokenProvider;
	@MockBean
	JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockBean(name = "memberTaskExecutor")
	AsyncTaskExecutor memberTaskExecutor;

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
	@DisplayName("공개 프로필 조회시 해당 회원이 없다면 400과 Problem code 확인")
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
	@WithMockUser(username = "1")
	void test03() throws Exception {
		given(memberProfileRetrieval.getProfile(1L))
			.willReturn(new MemberProfileRetrieval.Response("img", "intro"));

		MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/profile"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.profileImageUrl").value("img"));

		// Mock 검증 강화 - 정확한 memberId로 호출되었는지 확인
		verify(memberProfileRetrieval).getProfile(1L);
	}

	@Test
	@DisplayName("내 프로필 조회 - 미인증 시 401 반환")
	void test04() throws Exception {
		// 인증 실패 시 Controller 진입 전 예외 발생으로 비동기 처리 시작 안됨
		mockMvc.perform(get("/api/v1/members/profile"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("프로필 수정 - 잘못된 principal(문자) 401")
	@WithMockUser(username = "abc")
	void test05() throws Exception {
		// NumberFormatException 발생시 Controller에서 401 ResponseStatusException 발생
		mockMvc.perform(
				post("/api/v1/members/profile")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"profileImageUrl\":\"img\",\"selfIntroduction\":\"intro\"}")
			)
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("프로필 수정 - 인증 성공 200")
	@WithMockUser(username = "1")
	void test06() throws Exception {
		given(memberProfileUpdate.updateProfile(eq(1L), any(MemberProfileUpdate.Request.class)))
			.willReturn(new MemberProfileUpdate.Response(1L));

		MvcResult mvcResult = mockMvc.perform(post("/api/v1/members/profile")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"profileImageUrl\":\"img\",\"selfIntroduction\":\"intro\"}"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberId").value(1));

		// Mock 검증 강화 - 정확한 파라미터로 호출되었는지 확인
		verify(memberProfileUpdate).updateProfile(eq(1L), argThat(req ->
			"img".equals(req.profileImageUrl()) &&
				"intro".equals(req.selfIntroduction())
		));
	}

}

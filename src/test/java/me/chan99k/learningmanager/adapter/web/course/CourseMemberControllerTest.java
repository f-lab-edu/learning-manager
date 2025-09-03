package me.chan99k.learningmanager.adapter.web.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.AfterEach;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.adapter.auth.AccessTokenProvider;
import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.course.CourseMemberService;
import me.chan99k.learningmanager.application.course.provides.CourseMemberAddition;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.CourseProblemCode;
import me.chan99k.learningmanager.domain.course.CourseRole;

@WebMvcTest(controllers = CourseMemberController.class)
@Import(GlobalExceptionHandler.class)
class CourseMemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CourseMemberService courseMemberService;

	@MockBean
	private AccessTokenProvider<Long> accessTokenProvider;

	@MockBean(name = "courseTaskExecutor")
	private Executor courseTaskExecutor;

	@BeforeEach
	void setUp() {
		// 비동기 작업을 동기적으로 실행하도록 설정
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(courseTaskExecutor).execute(any(Runnable.class));

		// 토큰 검증 모킹
		when(accessTokenProvider.validateAccessToken("valid-token")).thenReturn(true);
		when(accessTokenProvider.getIdFromAccessToken("valid-token")).thenReturn(1L);

		// 모든 테스트에서 기본적으로 인증된 사용자가 있도록 설정
		AuthenticationContextHolder.setCurrentMemberId(1L);
	}

	@AfterEach
	void tearDown() {
		AuthenticationContextHolder.clear();
	}

	@Test
	@DisplayName("[Success] 단일 멤버 추가 요청이 성공하면 200 OK를 반환한다")
	void addSingleMember_Success() throws Exception {
		// given
		CourseMemberAddition.Request request = new CourseMemberAddition.Request(List.of(
			new CourseMemberAddition.MemberAdditionItem("add@example.com", CourseRole.MENTEE)
		));
		// 단일 요청이므로 addSingleMember 메서드가 호출됨 (void 반환)
		doNothing().when(courseMemberService)
			.addSingleMember(anyLong(), any(CourseMemberAddition.MemberAdditionItem.class));

		// when
		MvcResult mvcResult = mockMvc.perform(post("/api/v1/courses/{courseId}/members", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalCount").value(1))
			.andExpect(jsonPath("$.successCount").value(1))
			.andExpect(jsonPath("$.failureCount").value(0));
	}

	@Test
	@DisplayName("[Failure] 요청 본문이 유효하지 않으면 400 Bad Request를 반환한다")
	void addMember_Fail_InvalidRequest() throws Exception {
		// given
		CourseMemberAddition.Request request = new CourseMemberAddition.Request(List.of());

		// when & then
		mockMvc.perform(post("/api/v1/courses/{courseId}/members", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Validation Error"));
	}

	@Test
	@DisplayName("[Success] 벌크 멤버 추가 요청이 성공하면 207 Multi-Status를 반환한다")
	void addBulkMembers_Success() throws Exception {
		// given
		CourseMemberAddition.Request request = new CourseMemberAddition.Request(List.of(
			new CourseMemberAddition.MemberAdditionItem("member1@example.com", CourseRole.MENTEE),
			new CourseMemberAddition.MemberAdditionItem("member2@example.com", CourseRole.MENTEE)
		));
		CourseMemberAddition.Response response = new CourseMemberAddition.Response(
			2, 2, 0,
			List.of(
				new CourseMemberAddition.MemberResult("member1@example.com", CourseRole.MENTEE, "SUCCESS",
					"과정 멤버 추가 성공"),
				new CourseMemberAddition.MemberResult("member2@example.com", CourseRole.MENTEE, "SUCCESS",
					"과정 멤버 추가 성공")
			)
		);
		when(courseMemberService.addMultipleMembers(anyLong(), anyList())).thenReturn(response);

		// when
		MvcResult mvcResult = mockMvc.perform(post("/api/v1/courses/{courseId}/members", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isMultiStatus())
			.andExpect(jsonPath("$.totalCount").value(2))
			.andExpect(jsonPath("$.successCount").value(2))
			.andExpect(jsonPath("$.failureCount").value(0));
	}

	@Test
	@DisplayName("[Failure] 단일 멤버 추가에서 권한 없음 예외 발생 시 403 Forbidden을 반환한다")
	void addSingleMember_Fail_Authorization() throws Exception {
		// given
		CourseMemberAddition.Request request = new CourseMemberAddition.Request(List.of(
			new CourseMemberAddition.MemberAdditionItem("add@example.com", CourseRole.MENTEE)
		));
		doThrow(new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED))
			.when(courseMemberService).addSingleMember(anyLong(), any(CourseMemberAddition.MemberAdditionItem.class));

		// when
		MvcResult mvcResult = mockMvc.perform(post("/api/v1/courses/{courseId}/members", 1L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(AuthProblemCode.AUTHORIZATION_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("[Failure] 단일 멤버 추가에서 도메인 예외(과정 없음) 발생 시 400 Bad Request를 반환한다")
	void addSingleMember_Fail_CourseNotFound() throws Exception {
		// given
		CourseMemberAddition.Request request = new CourseMemberAddition.Request(List.of(
			new CourseMemberAddition.MemberAdditionItem("add@example.com", CourseRole.MENTEE)
		));
		doThrow(new DomainException(CourseProblemCode.COURSE_NOT_FOUND))
			.when(courseMemberService).addSingleMember(anyLong(), any(CourseMemberAddition.MemberAdditionItem.class));

		MvcResult mvcResult = mockMvc.perform(post("/api/v1/courses/{courseId}/members", 999L)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(CourseProblemCode.COURSE_NOT_FOUND.getCode()));
	}
}

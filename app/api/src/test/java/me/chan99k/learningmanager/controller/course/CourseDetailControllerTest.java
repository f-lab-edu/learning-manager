package me.chan99k.learningmanager.controller.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.controller.BaseControllerTest;
import me.chan99k.learningmanager.course.CourseDetailInfo;
import me.chan99k.learningmanager.course.CourseDetailRetrieval;
import me.chan99k.learningmanager.course.CourseMemberInfo;
import me.chan99k.learningmanager.course.CourseRole;

@WebMvcTest(controllers = CourseDetailController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourseDetailControllerTest extends BaseControllerTest {

	private static final Long MEMBER_ID = 1L;
	private static final Long COURSE_ID = 1L;

	@MockBean
	private CourseDetailRetrieval courseDetailRetrieval;

	@MockBean(name = "courseTaskExecutor")
	private Executor courseTaskExecutor;

	@Autowired
	protected CourseDetailControllerTest(MockMvc mockMvc,
		ObjectMapper objectMapper) {
		super(mockMvc, objectMapper);
	}

	@BeforeEach
	void setUp() {
		// Executor가 즉시 실행되도록 스터빙 (비동기 테스트를 동기적으로 수행)
		willAnswer((Answer<Void>)invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).given(courseTaskExecutor).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("[Success] 과정 상세 정보 조회가 정상 동작한다")
	void getCourseDetail() throws Exception {
		CourseDetailInfo courseDetail = new CourseDetailInfo(
			COURSE_ID,
			"Spring Boot 기초",
			"스프링 부트 학습 과정",
			Instant.parse("2025-01-01T00:00:00Z"),
			10L,
			5L
		);
		CourseDetailRetrieval.CourseDetailResponse response =
			new CourseDetailRetrieval.CourseDetailResponse(courseDetail);

		given(courseDetailRetrieval.getCourseDetail(COURSE_ID)).willReturn(response);

		// 비동기 컨트롤러 테스트: 1단계 - 비동기 요청 시작
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/courses/{courseId}", COURSE_ID)
				.with(user(createMockUser(MEMBER_ID))))
			.andExpect(request().asyncStarted())
			.andReturn();

		// 비동기 컨트롤러 테스트: 2단계 - 비동기 결과 검증
		mockMvc.perform(asyncDispatch(mvcResult))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.courseDetail.courseId").value(COURSE_ID))
			.andExpect(jsonPath("$.courseDetail.title").value("Spring Boot 기초"))
			.andExpect(jsonPath("$.courseDetail.description").value("스프링 부트 학습 과정"))
			.andExpect(jsonPath("$.courseDetail.totalMembers").value(10))
			.andExpect(jsonPath("$.courseDetail.totalCurricula").value(5));

		then(courseDetailRetrieval).should().getCourseDetail(COURSE_ID);
	}

	@Test
	@DisplayName("[Success] 과정 멤버 목록 조회가 정상 동작한다")
	void getCourseMembers() throws Exception {
		Instant joinedAt = Instant.parse("2025-01-01T00:00:00Z");
		List<CourseMemberInfo> members = List.of(
			new CourseMemberInfo(1L, "사용자1", "user1@test.com", CourseRole.MENTEE, joinedAt),
			new CourseMemberInfo(2L, "사용자2", "user2@test.com", CourseRole.MANAGER, joinedAt)
		);
		PageResult<CourseMemberInfo> memberPage = PageResult.of(
			members,
			PageRequest.of(0, 20),
			2
		);

		given(courseDetailRetrieval.getCourseMembers(eq(COURSE_ID), any(PageRequest.class)))
			.willReturn(memberPage);

		// 비동기 컨트롤러 테스트: 1단계 - 비동기 요청 시작
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/courses/{courseId}/members", COURSE_ID)
				.with(user(createMockUser(MEMBER_ID)))
				.param("page", "0")
				.param("size", "20"))
			.andExpect(request().asyncStarted())
			.andReturn();

		// 비동기 컨트롤러 테스트: 2단계 - 비동기 결과 검증
		mockMvc.perform(asyncDispatch(mvcResult))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.content[0].nickname").value("사용자1"))
			.andExpect(jsonPath("$.content[0].courseRole").value("MENTEE"))
			.andExpect(jsonPath("$.content[1].nickname").value("사용자2"))
			.andExpect(jsonPath("$.content[1].courseRole").value("MANAGER"))
			.andExpect(jsonPath("$.totalElements").value(2));

		then(courseDetailRetrieval).should().getCourseMembers(eq(COURSE_ID), any(PageRequest.class));
	}

	// Note: 보안 필터가 비활성화된 상태에서 인증 테스트는 통합 테스트에서 수행
}

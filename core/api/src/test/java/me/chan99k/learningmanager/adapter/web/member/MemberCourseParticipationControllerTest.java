package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.Arrays;
import java.util.Collections;
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

import me.chan99k.learningmanager.application.auth.UserContext;
import me.chan99k.learningmanager.application.member.CourseParticipationInfo;
import me.chan99k.learningmanager.application.member.MemberCourseParticipationService;
import me.chan99k.learningmanager.application.member.MemberCourseParticipationService.ParticipatingCoursesResponse;
import me.chan99k.learningmanager.domain.course.CourseRole;
import me.chan99k.learningmanager.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.web.member.MemberCourseParticipationController;

@WebMvcTest(controllers = MemberCourseParticipationController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class MemberCourseParticipationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MemberCourseParticipationService memberCourseParticipationService;

	@MockBean
	private UserContext userContext;

	@MockBean(name = "memberTaskExecutor")
	private Executor memberTaskExecutor;

	@BeforeEach
	void setUp() {
		// 비동기 작업을 동기적으로 실행하도록 설정
		doAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).when(memberTaskExecutor).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("멤버가 참여한 과정 목록을 성공적으로 조회한다")
	void getParticipatingCourses_success() throws Exception {
		// given
		Long memberId = 1L;

		CourseParticipationInfo course1 = new CourseParticipationInfo(
			1L, "Java 기초", "Java 프로그래밍 기초 과정", CourseRole.MANAGER
		);
		CourseParticipationInfo course2 = new CourseParticipationInfo(
			2L, "Spring Boot", "Spring Boot 웹 개발 과정", CourseRole.MENTEE
		);

		ParticipatingCoursesResponse mockResponse = new ParticipatingCoursesResponse(
			Arrays.asList(course1, course2)
		);

		when(memberCourseParticipationService.getParticipatingCourses(memberId))
			.thenReturn(mockResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/members/{memberId}/courses", memberId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.courses").isArray())
			.andExpect(jsonPath("$.courses.length()").value(2))
			.andExpect(jsonPath("$.courses[0].courseId").value(1))
			.andExpect(jsonPath("$.courses[0].title").value("Java 기초"))
			.andExpect(jsonPath("$.courses[0].description").value("Java 프로그래밍 기초 과정"))
			.andExpect(jsonPath("$.courses[0].role").value("MANAGER"))
			.andExpect(jsonPath("$.courses[1].courseId").value(2))
			.andExpect(jsonPath("$.courses[1].title").value("Spring Boot"))
			.andExpect(jsonPath("$.courses[1].description").value("Spring Boot 웹 개발 과정"))
			.andExpect(jsonPath("$.courses[1].role").value("MENTEE"));

		verify(memberCourseParticipationService).getParticipatingCourses(memberId);
	}

	@Test
	@DisplayName("참여한 과정이 없는 경우 빈 배열을 반환한다")
	void getParticipatingCourses_whenNoCourses() throws Exception {
		// given
		Long memberId = 1L;
		ParticipatingCoursesResponse mockResponse = new ParticipatingCoursesResponse(
			Collections.emptyList()
		);

		when(memberCourseParticipationService.getParticipatingCourses(memberId))
			.thenReturn(mockResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/members/{memberId}/courses", memberId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.courses").isArray())
			.andExpect(jsonPath("$.courses.length()").value(0));

		verify(memberCourseParticipationService).getParticipatingCourses(memberId);
	}

	@Test
	@DisplayName("잘못된 memberId 형식에 대해 500 에러를 반환한다")
	void getParticipatingCourses_invalidMemberId() throws Exception {
		// when & then
		mockMvc.perform(get("/members/{memberId}/courses", "invalid")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isInternalServerError())
			.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
			.andExpect(jsonPath("$.status").value(500))
			.andExpect(jsonPath("$.detail").value("[System] 일시적인 서버 오류가 발생했습니다."));

		verifyNoInteractions(memberCourseParticipationService);
	}
}
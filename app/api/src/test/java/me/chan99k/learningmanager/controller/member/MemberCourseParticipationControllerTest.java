package me.chan99k.learningmanager.controller.member;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.advice.GlobalExceptionHandler;
import me.chan99k.learningmanager.controller.BaseControllerTest;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.member.CourseParticipationInfo;
import me.chan99k.learningmanager.member.MemberCourseParticipation;
import me.chan99k.learningmanager.security.CustomUserDetails;

@WebMvcTest(controllers = MemberCourseParticipationController.class)
@Import(GlobalExceptionHandler.class)
class MemberCourseParticipationControllerTest extends BaseControllerTest {

	private static final Long MEMBER_ID = 1L;

	@MockBean
	private MemberCourseParticipation memberCourseParticipation;

	@MockBean(name = "memberTaskExecutor")
	private Executor memberTaskExecutor;

	@Autowired
	protected MemberCourseParticipationControllerTest(MockMvc mockMvc,
		ObjectMapper objectMapper) {
		super(mockMvc, objectMapper);
	}

	private CustomUserDetails createMockUser() {
		return new CustomUserDetails(
			MEMBER_ID,
			"test@example.com",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
	}

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
		CourseParticipationInfo course1 = new CourseParticipationInfo(
			1L, "Java 기초", "Java 프로그래밍 기초 과정", CourseRole.MANAGER
		);
		CourseParticipationInfo course2 = new CourseParticipationInfo(
			2L, "Spring Boot", "Spring Boot 웹 개발 과정", CourseRole.MENTEE
		);

		MemberCourseParticipation.Response mockResponse = new MemberCourseParticipation.Response(
			Arrays.asList(course1, course2)
		);

		when(memberCourseParticipation.getParticipatingCourses(MEMBER_ID))
			.thenReturn(mockResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/me/courses")
				.with(user(createMockUser()))
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

		verify(memberCourseParticipation).getParticipatingCourses(MEMBER_ID);
	}

	@Test
	@DisplayName("참여한 과정이 없는 경우 빈 배열을 반환한다")
	void getParticipatingCourses_whenNoCourses() throws Exception {
		// given
		MemberCourseParticipation.Response mockResponse = new MemberCourseParticipation.Response(
			Collections.emptyList()
		);

		when(memberCourseParticipation.getParticipatingCourses(MEMBER_ID))
			.thenReturn(mockResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/me/courses")
				.with(user(createMockUser()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.courses").isArray())
			.andExpect(jsonPath("$.courses.length()").value(0));

		verify(memberCourseParticipation).getParticipatingCourses(MEMBER_ID);
	}
}

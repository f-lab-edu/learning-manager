package me.chan99k.learningmanager.adapter.web.session;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import me.chan99k.learningmanager.adapter.web.GlobalExceptionHandler;
import me.chan99k.learningmanager.application.auth.requires.UserContext;
import me.chan99k.learningmanager.application.session.SessionCreationService;
import me.chan99k.learningmanager.application.session.provides.SessionDeletion;
import me.chan99k.learningmanager.application.session.provides.SessionListRetrieval;
import me.chan99k.learningmanager.application.session.provides.SessionUpdate;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

@WebMvcTest(controllers = SessionController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@Import({
	GlobalExceptionHandler.class
})
class SessionListControllerTest {

	@MockBean
	UserContext userContext;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SessionCreationService sessionCreationService;

	@MockBean
	private SessionUpdate sessionUpdate;

	@MockBean
	private SessionDeletion sessionDeletion;

	@MockBean
	private SessionListRetrieval sessionListRetrieval;

	@MockBean
	private SessionQueryRepository sessionQueryRepository;

	@MockBean
	private TaskExecutor sessionTaskExecutor;

	@BeforeEach
	void setUp() {
		given(userContext.getCurrentMemberId()).willReturn(1L);
		given(userContext.isAuthenticated()).willReturn(true);

		// TaskExecutor가 동기적으로 실행하도록 설정 - CompletableFuture.supplyAsync 지원
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			try {
				task.run();
			} catch (Exception e) {
				throw new RuntimeException("TaskExecutor execution failed", e);
			}
			return null;
		}).given(sessionTaskExecutor).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("전체 세션 목록 조회 API - 성공")
	void getSessionList_Success() throws Exception {
		// given
		var sessionResponse = new SessionListRetrieval.SessionListResponse(
			1L, "테스트 세션",
			Instant.parse("2025-01-01T10:00:00Z"),
			Instant.parse("2025-01-01T12:00:00Z"),
			SessionType.ONLINE, SessionLocation.ZOOM, null,
			100L, 200L, null, 0, 5, SessionListRetrieval.SessionStatus.UPCOMING
		);
		var pageResponse = new PageImpl<>(List.of(sessionResponse), PageRequest.of(0, 20), 1);

		when(sessionListRetrieval.getSessionList(any())).thenReturn(pageResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/sessions")
				.param("page", "0")
				.param("size", "20")
				.param("sort", "scheduledAt,desc"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content[0].id").value(1))
			.andExpect(jsonPath("$.content[0].title").value("테스트 세션"))
			.andExpect(jsonPath("$.content[0].type").value("ONLINE"))
			.andExpect(jsonPath("$.content[0].status").value("UPCOMING"))
			.andExpect(jsonPath("$.totalElements").value(1))
			.andExpect(jsonPath("$.totalPages").value(1));

		verify(sessionListRetrieval).getSessionList(argThat(request ->
			request.page() == 0 && request.size() == 20 &&
				"scheduledAt,desc".equals(request.sort())
		));
	}

	@Test
	@DisplayName("전체 세션 목록 조회 API - 필터링")
	void getSessionList_WithFilters() throws Exception {
		// given
		var sessionResponse = new SessionListRetrieval.SessionListResponse(
			1L, "오프라인 세션",
			Instant.parse("2025-01-01T10:00:00Z"),
			Instant.parse("2025-01-01T12:00:00Z"),
			SessionType.OFFLINE, SessionLocation.SITE, "서울시 강남구",
			100L, 200L, null, 2, 10, SessionListRetrieval.SessionStatus.UPCOMING
		);
		var pageResponse = new PageImpl<>(List.of(sessionResponse), PageRequest.of(0, 20), 1);

		when(sessionListRetrieval.getSessionList(any())).thenReturn(pageResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/sessions")
				.param("type", "OFFLINE")
				.param("location", "SITE")
				.param("startDate", "2025-01-01T09:00:00Z")
				.param("endDate", "2025-01-01T18:00:00Z"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].type").value("OFFLINE"))
			.andExpect(jsonPath("$.content[0].location").value("SITE"));

		verify(sessionListRetrieval).getSessionList(argThat(request ->
			request.type() == SessionType.OFFLINE &&
				request.location() == SessionLocation.SITE &&
				request.startDate() != null && request.endDate() != null
		));
	}

	@Test
	@DisplayName("과정별 세션 목록 조회 API - 성공")
	void getCourseSessionList_Success() throws Exception {
		// given
		Long courseId = 100L;
		var sessionResponse = new SessionListRetrieval.SessionListResponse(
			1L, "과정 세션",
			Instant.parse("2025-01-01T10:00:00Z"),
			Instant.parse("2025-01-01T12:00:00Z"),
			SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null,
			courseId, null, null, 0, 3, SessionListRetrieval.SessionStatus.ONGOING
		);
		var pageResponse = new PageImpl<>(List.of(sessionResponse), PageRequest.of(0, 20), 1);

		when(sessionListRetrieval.getCourseSessionList(eq(courseId), any())).thenReturn(pageResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/sessions/courses/{courseId}", courseId)
				.param("includeChildSessions", "true"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].courseId").value(courseId))
			.andExpect(jsonPath("$.content[0].status").value("ONGOING"));

		verify(sessionListRetrieval).getCourseSessionList(eq(courseId), argThat(request ->
			request.includeChildSessions() == true
		));
	}

	@Test
	@DisplayName("커리큘럼별 세션 목록 조회 API - 성공")
	void getCurriculumSessionList_Success() throws Exception {
		// given
		Long curriculumId = 200L;
		var sessionResponse = new SessionListRetrieval.SessionListResponse(
			1L, "커리큘럼 세션",
			Instant.parse("2025-01-01T10:00:00Z"),
			Instant.parse("2025-01-01T12:00:00Z"),
			SessionType.OFFLINE, SessionLocation.SITE, "부산시 해운대구",
			100L, curriculumId, null, 1, 8, SessionListRetrieval.SessionStatus.COMPLETED
		);
		var pageResponse = new PageImpl<>(List.of(sessionResponse), PageRequest.of(1, 10), 11);

		when(sessionListRetrieval.getCurriculumSessionList(eq(curriculumId), any())).thenReturn(pageResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/sessions/curricula/{curriculumId}", curriculumId)
				.param("page", "1")
				.param("size", "10")
				.param("includeChildSessions", "false"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].curriculumId").value(curriculumId))
			.andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
			.andExpect(jsonPath("$.totalElements").value(11));

		verify(sessionListRetrieval).getCurriculumSessionList(eq(curriculumId), argThat(request ->
			request.page() == 1 && request.size() == 10 &&
				request.includeChildSessions() == false
		));
	}

	@Test
	@DisplayName("세션 목록 조회 - 기본값 적용")
	void getSessionList_DefaultParameters() throws Exception {
		// given
		var sessionResponse = new SessionListRetrieval.SessionListResponse(
			1L, "기본 세션",
			Instant.parse("2025-01-01T10:00:00Z"),
			Instant.parse("2025-01-01T12:00:00Z"),
			SessionType.ONLINE, SessionLocation.ZOOM, null,
			null, null, null, 0, 1, SessionListRetrieval.SessionStatus.UPCOMING
		);
		var pageResponse = new PageImpl<>(List.of(sessionResponse), PageRequest.of(0, 20), 1);

		when(sessionListRetrieval.getSessionList(any())).thenReturn(pageResponse);

		// when & then
		MvcResult mvcResult = mockMvc.perform(get("/api/v1/sessions"))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk());

		// 기본값들이 적용되었는지 확인
		verify(sessionListRetrieval).getSessionList(argThat(request ->
			request.page() == 0 &&
				request.size() == 20 &&
				"scheduledAt,desc".equals(request.sort()) &&
				request.type() == null &&
				request.location() == null
		));
	}
}
package me.chan99k.learningmanager.controller.session;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.advice.GlobalExceptionHandler;
import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.security.CustomUserDetails;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionCreation;
import me.chan99k.learningmanager.session.SessionDeletion;
import me.chan99k.learningmanager.session.SessionListRetrieval;
import me.chan99k.learningmanager.session.SessionLocation;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;
import me.chan99k.learningmanager.session.SessionType;
import me.chan99k.learningmanager.session.SessionUpdate;

// NOTE :: 단위 테스트로 변경
@WebMvcTest(controllers = SessionController.class)
@Import(GlobalExceptionHandler.class)
class SessionControllerTest {

	private static final Long MEMBER_ID = 1L;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	SessionCreation sessionCreation;

	@MockBean
	SessionUpdate sessionUpdate;

	@MockBean
	SessionDeletion sessionDeletion;

	@MockBean
	SessionListRetrieval sessionListRetrieval;

	@MockBean
	SessionQueryRepository sessionQueryRepository;

	@MockBean(name = "sessionTaskExecutor")
	AsyncTaskExecutor sessionTaskExecutor;

	@MockBean
	JwtProvider jwtProvider;

	@MockBean
	SystemAuthorizationPort systemAuthorizationPort;

	private CustomUserDetails createMockUser() {
		return new CustomUserDetails(
			MEMBER_ID,
			"test@example.com",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
	}

	@BeforeEach
	void setUp() {
		willAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run(); // 즉시 실행
			return null;
		}).given(sessionTaskExecutor).execute(any(Runnable.class));
	}

	@Test
	@DisplayName("[Success] 세션 생성 요청이 성공한다")
	void createSession_Success() throws Exception {
		Long requestedBy = 1L;
		SessionCreation.Request request = new SessionCreation.Request(
			requestedBy, null, null, null,
			"테스트 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, "Zoom 링크"
		);

		Session mockSession = mock(Session.class);
		when(mockSession.getId()).thenReturn(1L);
		when(mockSession.getTitle()).thenReturn("테스트 세션");
		when(mockSession.getScheduledAt()).thenReturn(request.scheduledAt());
		when(mockSession.getScheduledEndAt()).thenReturn(request.scheduledEndAt());
		when(mockSession.getType()).thenReturn(SessionType.ONLINE);
		when(mockSession.getLocation()).thenReturn(SessionLocation.ZOOM);
		when(mockSession.getLocationDetails()).thenReturn("Zoom 링크");
		when(mockSession.getCourseId()).thenReturn(null);
		when(mockSession.getCurriculumId()).thenReturn(null);

		given(sessionCreation.createSession(any(SessionCreation.Request.class)))
			.willReturn(mockSession);

		mockMvc.perform(post("/api/v1/sessions")
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.title").value("테스트 세션"))
			.andExpect(jsonPath("$.type").value("ONLINE"))
			.andExpect(jsonPath("$.location").value("ZOOM"))
			.andExpect(jsonPath("$.locationDetails").value("Zoom 링크"));
	}

	@Test
	@DisplayName("[Failure] 권한이 없는 사용자의 세션 생성 시 403 응답")
	void createSession_AuthorizationFail() throws Exception {
		Long requestedBy = 1L;
		SessionCreation.Request request = new SessionCreation.Request(
			requestedBy, null, null, null,
			"테스트 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, "Zoom 링크"
		);

		given(sessionCreation.createSession(any(SessionCreation.Request.class)))
			.willThrow(new DomainException(MemberProblemCode.ADMIN_ONLY_ACTION));

		mockMvc.perform(post("/api/v1/sessions")
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("[Failure] 도메인 예외 발생 시 400 응답")
	void createSession_DomainException() throws Exception {
		Long requestedBy = 1L;
		SessionCreation.Request request = new SessionCreation.Request(
			requestedBy, null, null, null,
			"테스트 세션",
			LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, "Zoom 링크"
		);

		given(sessionCreation.createSession(any(SessionCreation.Request.class)))
			.willThrow(new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		mockMvc.perform(post("/api/v1/sessions")
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Success] 세션 조회가 성공한다")
	void getSession_Success() throws Exception {
		Long sessionId = 1L;
		Session mockSession = mock(Session.class);
		when(mockSession.getId()).thenReturn(sessionId);
		when(mockSession.getTitle()).thenReturn("테스트 세션");
		when(mockSession.getScheduledAt()).thenReturn(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC));
		when(mockSession.getScheduledEndAt()).thenReturn(
			LocalDateTime.now().plusDays(1).plusHours(2).toInstant(ZoneOffset.UTC));
		when(mockSession.getType()).thenReturn(SessionType.ONLINE);
		when(mockSession.getLocation()).thenReturn(SessionLocation.ZOOM);
		when(mockSession.getLocationDetails()).thenReturn("Zoom 링크");
		when(mockSession.getCourseId()).thenReturn(null);
		when(mockSession.getCurriculumId()).thenReturn(null);
		when(mockSession.getParent()).thenReturn(null);
		when(mockSession.getChildren()).thenReturn(List.of());
		when(mockSession.getParticipants()).thenReturn(List.of());

		given(sessionQueryRepository.findById(sessionId)).willReturn(Optional.of(mockSession));

		MvcResult mvcResult = mockMvc.perform(get("/api/v1/sessions/{id}", sessionId)
				.with(user(createMockUser())))
			.andDo(print())
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(sessionId))
			.andExpect(jsonPath("$.title").value("테스트 세션"))
			.andExpect(jsonPath("$.type").value("ONLINE"))
			.andExpect(jsonPath("$.location").value("ZOOM"))
			.andExpect(jsonPath("$.locationDetails").value("Zoom 링크"))
			.andExpect(jsonPath("$.childrenCount").value(0))
			.andExpect(jsonPath("$.participantCount").value(0));
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션 조회 시 400 응답")
	void getSession_NotFound() throws Exception {
		Long invalidSessionId = 999L;

		given(sessionQueryRepository.findById(invalidSessionId)).willReturn(Optional.empty());

		MvcResult mvcResult = mockMvc.perform(get("/api/v1/sessions/{id}", invalidSessionId)
				.with(user(createMockUser())))
			.andDo(print())
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(asyncDispatch(mvcResult))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 잘못된 요청 데이터로 세션 생성 시 400 응답")
	void createSession_InvalidRequest() throws Exception {
		String invalidRequest = "{\"title\":\"\"}";

		mockMvc.perform(post("/api/v1/sessions")
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Success] 세션 수정 요청이 성공하면 204 No Content를 반환한다")
	void updateSession_Success() throws Exception {
		Long sessionId = 1L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Updated Session Title",
			LocalDateTime.now().plusDays(7).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(7).plusHours(3).toInstant(ZoneOffset.UTC),
			SessionType.OFFLINE,
			SessionLocation.SITE,
			"Updated Room 101"
		);

		doNothing().when(sessionUpdate).updateSession(any(), eq(sessionId), eq(request));

		mockMvc.perform(put("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isNoContent());

		verify(sessionUpdate).updateSession(any(), eq(sessionId), eq(request));
	}

	@Test
	@DisplayName("[Failure] 권한이 없는 사용자의 세션 수정 시 403 응답")
	void updateSession_AuthorizationFail() throws Exception {
		Long sessionId = 1L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null
		);

		doThrow(new DomainException(CourseProblemCode.NOT_COURSE_MANAGER))
			.when(sessionUpdate).updateSession(any(), eq(sessionId), eq(request));

		mockMvc.perform(put("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(CourseProblemCode.NOT_COURSE_MANAGER.getCode()));
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션 수정 시 400 응답")
	void updateSession_SessionNotFound() throws Exception {
		Long sessionId = 999L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Title", LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(1).plusHours(1).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE, SessionLocation.ZOOM, null
		);

		doThrow(new DomainException(SessionProblemCode.SESSION_NOT_FOUND))
			.when(sessionUpdate).updateSession(any(), eq(sessionId), eq(request));

		mockMvc.perform(put("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Failure] 잘못된 요청 데이터로 세션 수정 시 400 응답")
	void updateSession_InvalidRequest() throws Exception {
		Long sessionId = 1L;
		String invalidRequest = "{\"title\":\"\"}";

		mockMvc.perform(put("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[Success] 유효한 세션ID로 수정 요청이 성공한다")
	void updateSession_ValidSessionId_Success() throws Exception {
		Long sessionId = 123L;
		SessionUpdate.Request request = new SessionUpdate.Request(
			"Valid Session Title",
			LocalDateTime.now().plusDays(5).toInstant(ZoneOffset.UTC),
			LocalDateTime.now().plusDays(5).plusHours(2).toInstant(ZoneOffset.UTC),
			SessionType.ONLINE,
			SessionLocation.ZOOM,
			null
		);

		doNothing().when(sessionUpdate).updateSession(any(), eq(sessionId), eq(request));

		mockMvc.perform(put("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isNoContent());

		verify(sessionUpdate).updateSession(any(), eq(sessionId), eq(request));
	}

	@Test
	@DisplayName("[Success] 세션 삭제 요청이 성공하면 204 No Content를 반환한다")
	void deleteSession_Success() throws Exception {
		Long sessionId = 1L;

		doNothing().when(sessionDeletion).deleteSession(any(), eq(sessionId));

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isNoContent());

		verify(sessionDeletion).deleteSession(any(), eq(sessionId));
	}

	@Test
	@DisplayName("[Failure] 권한이 없는 사용자의 세션 삭제 시 403 응답")
	void deleteSession_AuthorizationFail() throws Exception {
		Long sessionId = 1L;

		doThrow(new DomainException(CourseProblemCode.NOT_COURSE_MANAGER))
			.when(sessionDeletion).deleteSession(any(), eq(sessionId));

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(CourseProblemCode.NOT_COURSE_MANAGER.getCode()));
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션 삭제 시 400 응답")
	void deleteSession_SessionNotFound() throws Exception {
		Long sessionId = 999L;

		doThrow(new DomainException(SessionProblemCode.SESSION_NOT_FOUND))
			.when(sessionDeletion).deleteSession(any(), eq(sessionId));

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(SessionProblemCode.SESSION_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("[Failure] 하위 세션이 있는 세션 삭제 시 400 응답")
	void deleteSession_SessionWithChildren() throws Exception {
		Long sessionId = 1L;

		doThrow(new IllegalArgumentException(SessionProblemCode.CANNOT_DELETE_WHEN_CHILD_EXISTS.getMessage()))
			.when(sessionDeletion).deleteSession(any(), eq(sessionId));

		mockMvc.perform(delete("/api/v1/sessions/{sessionId}", sessionId)
				.with(user(createMockUser()))
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}
}

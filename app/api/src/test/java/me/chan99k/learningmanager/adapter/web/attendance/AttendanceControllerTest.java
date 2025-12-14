package me.chan99k.learningmanager.adapter.web.attendance;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import me.chan99k.learningmanager.attendance.AttendanceRetrieval;
import me.chan99k.learningmanager.attendance.AttendanceStatus;
import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.controller.attendance.AttendanceController;
import me.chan99k.learningmanager.security.CustomUserDetails;

@WebMvcTest(controllers = AttendanceController.class)
@DisplayName("AttendanceController 테스트")
class AttendanceControllerTest {

	private static final Long MEMBER_ID = 123L;
	private static final Long COURSE_ID = 456L;
	private static final Long CURRICULUM_ID = 789L;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AttendanceRetrieval attendanceRetrieval;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private SystemAuthorizationPort systemAuthorizationPort;

	private CustomUserDetails createMockUser() {
		return new CustomUserDetails(
			MEMBER_ID,
			"test@example.com",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
	}

	@Test
	@DisplayName("내 전체 출석 현황 조회 - 성공")
	void getMyAllAttendanceStatus_Success() throws Exception {
		AttendanceRetrieval.Response mockResponse = createMockResponse();
		when(attendanceRetrieval.getMyAllAttendanceStatus(any(AttendanceRetrieval.AllAttendanceRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/v1/attendance/status/my")
				.with(user(createMockUser())))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.sessions").isArray())
			.andExpect(jsonPath("$.sessions[0].attendanceId").value("attendance1"))
			.andExpect(jsonPath("$.sessions[0].sessionTitle").value("스프링 부트 기초"))
			.andExpect(jsonPath("$.sessions[0].finalStatus").value("PRESENT"))
			.andExpect(jsonPath("$.statistics.totalSessions").value(1))
			.andExpect(jsonPath("$.statistics.presentCount").value(1))
			.andExpect(jsonPath("$.statistics.attendanceRate").value(100.0));

		verify(attendanceRetrieval).getMyAllAttendanceStatus(
			argThat(req -> req.memberId().equals(MEMBER_ID))
		);
	}

	@Test
	@DisplayName("과정별 출석 현황 조회 - 성공")
	void getMyCourseAttendanceStatus_Success() throws Exception {
		AttendanceRetrieval.Response mockResponse = createMockResponse();
		when(attendanceRetrieval.getMyCourseAttendanceStatus(any(AttendanceRetrieval.CourseAttendanceRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/v1/attendance/status/my/course")
				.with(user(createMockUser()))
				.param("courseId", COURSE_ID.toString()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.sessions").exists())
			.andExpect(jsonPath("$.statistics").exists());

		verify(attendanceRetrieval).getMyCourseAttendanceStatus(
			argThat(req -> req.memberId().equals(MEMBER_ID) && req.courseId().equals(COURSE_ID))
		);
	}

	@Test
	@DisplayName("커리큘럼별 출석 현황 조회 - 성공")
	void getMyCurriculumAttendanceStatus_Success() throws Exception {
		AttendanceRetrieval.Response mockResponse = createMockResponse();
		when(attendanceRetrieval.getMyCurriculumAttendanceStatus(
			any(AttendanceRetrieval.CurriculumAttendanceRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/v1/attendance/status/my/curriculum")
				.with(user(createMockUser()))
				.param("curriculumId", CURRICULUM_ID.toString()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.sessions").exists())
			.andExpect(jsonPath("$.statistics").exists());

		verify(attendanceRetrieval).getMyCurriculumAttendanceStatus(
			argThat(req -> req.memberId().equals(MEMBER_ID) && req.curriculumId().equals(CURRICULUM_ID))
		);
	}

	@Test
	@DisplayName("월별 출석 현황 조회 - 성공")
	void getMyMonthlyAttendanceStatus_Success() throws Exception {
		int year = 2025;
		int month = 1;
		AttendanceRetrieval.Response mockResponse = createMockResponse();
		when(attendanceRetrieval.getMyMonthlyAttendanceStatus(any(AttendanceRetrieval.MonthlyAttendanceRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/v1/attendance/status/my/monthly")
				.with(user(createMockUser()))
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month))
				.param("courseId", COURSE_ID.toString())
				.param("curriculumId", CURRICULUM_ID.toString()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.sessions").exists())
			.andExpect(jsonPath("$.statistics").exists());

		verify(attendanceRetrieval).getMyMonthlyAttendanceStatus(
			argThat(req -> req.memberId().equals(MEMBER_ID)
				&& req.year() == year
				&& req.month() == month
				&& req.courseId().equals(COURSE_ID)
				&& req.curriculumId().equals(CURRICULUM_ID))
		);
	}

	@Test
	@DisplayName("기간별 출석 현황 조회 - 성공")
	void getMyPeriodAttendanceStatus_Success() throws Exception {
		String startDate = "2025-01-01T00:00:00Z";
		String endDate = "2025-01-31T23:59:59Z";
		String status = "PRESENT";

		AttendanceRetrieval.Response mockResponse = createMockResponse();
		when(attendanceRetrieval.getMyPeriodAttendanceStatus(any(AttendanceRetrieval.PeriodAttendanceRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/v1/attendance/status/my/period")
				.with(user(createMockUser()))
				.param("startDate", startDate)
				.param("endDate", endDate)
				.param("courseId", COURSE_ID.toString())
				.param("curriculumId", CURRICULUM_ID.toString())
				.param("status", status))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.sessions").exists())
			.andExpect(jsonPath("$.statistics").exists());

		verify(attendanceRetrieval).getMyPeriodAttendanceStatus(
			argThat(req -> req.memberId().equals(MEMBER_ID)
				&& req.startDate().equals(Instant.parse(startDate))
				&& req.endDate().equals(Instant.parse(endDate))
				&& req.courseId().equals(COURSE_ID)
				&& req.curriculumId().equals(CURRICULUM_ID)
				&& req.status().equals(AttendanceStatus.PRESENT))
		);
	}

	@Test
	@DisplayName("기간별 출석 현황 조회 - 필수 파라미터만 제공")
	void getMyPeriodAttendanceStatus_RequiredParamsOnly_Success() throws Exception {
		String startDate = "2025-01-01T00:00:00Z";
		String endDate = "2025-01-31T23:59:59Z";

		AttendanceRetrieval.Response mockResponse = createMockResponse();
		when(attendanceRetrieval.getMyPeriodAttendanceStatus(any(AttendanceRetrieval.PeriodAttendanceRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/v1/attendance/status/my/period")
				.with(user(createMockUser()))
				.param("startDate", startDate)
				.param("endDate", endDate))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON));

		verify(attendanceRetrieval).getMyPeriodAttendanceStatus(
			argThat(req -> req.memberId().equals(MEMBER_ID)
				&& req.startDate().equals(Instant.parse(startDate))
				&& req.endDate().equals(Instant.parse(endDate))
				&& req.courseId() == null
				&& req.curriculumId() == null
				&& req.status() == null)
		);
	}

	@Test
	@DisplayName("과정별 출석 현황 조회 - courseId 누락 시 400 에러")
	void getMyCourseAttendanceStatus_MissingCourseId_BadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/attendance/status/my/course")
				.with(user(createMockUser())))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(attendanceRetrieval);
	}

	@Test
	@DisplayName("커리큘럼별 출석 현황 조회 - curriculumId 누락 시 400 에러")
	void getMyCurriculumAttendanceStatus_MissingCurriculumId_BadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/attendance/status/my/curriculum")
				.with(user(createMockUser())))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(attendanceRetrieval);
	}

	@Test
	@DisplayName("기간별 출석 현황 조회 - startDate 누락 시 400 에러")
	void getMyPeriodAttendanceStatus_MissingStartDate_BadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/attendance/status/my/period")
				.with(user(createMockUser()))
				.param("endDate", "2025-01-31T23:59:59Z"))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(attendanceRetrieval);
	}

	private AttendanceRetrieval.Response createMockResponse() {
		AttendanceRetrieval.SessionAttendanceInfo sessionInfo =
			new AttendanceRetrieval.SessionAttendanceInfo(
				"attendance1",
				101L,
				"스프링 부트 기초",
				Instant.parse("2025-01-15T10:00:00Z"),
				AttendanceStatus.PRESENT,
				COURSE_ID,
				"백엔드 부트캠프",
				CURRICULUM_ID,
				"웹 개발 기초"
			);

		AttendanceRetrieval.AttendanceStatistics statistics =
			new AttendanceRetrieval.AttendanceStatistics(1, 1, 0, 0, 0, 100.0);

		return new AttendanceRetrieval.Response(List.of(sessionInfo), statistics);
	}
}

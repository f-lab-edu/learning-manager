package me.chan99k.learningmanager.controller.attendance;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.attendance.AttendanceCorrectionApproval;
import me.chan99k.learningmanager.attendance.AttendanceCorrectionRejection;
import me.chan99k.learningmanager.attendance.AttendanceCorrectionRequest;
import me.chan99k.learningmanager.attendance.AttendanceStatus;
import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.AttendanceSecurityPort;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.config.SecurityConfig;
import me.chan99k.learningmanager.controller.attendance.requests.AttendanceRejectionRequest;
import me.chan99k.learningmanager.controller.attendance.requests.CorrectionRequest;
import me.chan99k.learningmanager.filter.JwtAuthenticationFilter;
import me.chan99k.learningmanager.member.SystemRole;

/**
 * AttendanceCorrectionController 테스트
 *
 * @PreAuthorize("@attendanceSecurity.canRequestCorrection(...)") 등 검증을 포함합니다.
 */
@WebMvcTest(controllers = AttendanceCorrectionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("AttendanceCorrectionController 테스트")
class AttendanceCorrectionControllerTest {

	private static final Long MANAGER_ID = 1L;
	private static final Long NON_MANAGER_ID = 2L;
	private static final String ATTENDANCE_ID = "attendance-001";
	private static final String MANAGER_EMAIL = "manager@example.com";
	private static final String VALID_TOKEN = "valid-jwt-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AttendanceCorrectionRequest correctionRequest;

	@MockBean
	private AttendanceCorrectionApproval correctionApproval;

	@MockBean
	private AttendanceCorrectionRejection correctionRejection;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private SystemAuthorizationPort systemAuthorizationPort;

	@MockBean(name = "attendanceSecurity")
	private AttendanceSecurityPort attendanceSecurity;

	@BeforeEach
	void setUp() {
		// 기본 인증 설정: MANAGER_ID로 인증된 사용자
		JwtProvider.Claims claims = new JwtProvider.Claims(
			MANAGER_ID,
			MANAGER_EMAIL,
			Instant.now().plusSeconds(3600)
		);

		given(jwtProvider.isValid(VALID_TOKEN)).willReturn(true);
		given(jwtProvider.validateAndGetClaims(VALID_TOKEN)).willReturn(claims);
		given(systemAuthorizationPort.getRoles(MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));

		// 기본: MANAGER_ID는 출석 수정 요청/승인 권한 있음
		given(attendanceSecurity.canRequestCorrection(ATTENDANCE_ID, MANAGER_ID)).willReturn(true);
		given(attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, MANAGER_ID)).willReturn(true);
	}

	@Nested
	@DisplayName("출석 수정 요청 API (POST /api/v1/attendance/{attendanceId}/correction-requests)")
	class RequestCorrectionTest {

		@Test
		@DisplayName("[Success] 출석 수정 요청 성공 - 201 Created")
		void request_correction_success() throws Exception {
			CorrectionRequest request = new CorrectionRequest(AttendanceStatus.PRESENT, "지각이 아니라 출석입니다.");

			AttendanceCorrectionRequest.Response response = new AttendanceCorrectionRequest.Response(
				ATTENDANCE_ID,
				10L,
				100L,
				AttendanceStatus.LATE,
				AttendanceStatus.PRESENT,
				"지각이 아니라 출석입니다.",
				Instant.now()
			);

			given(correctionRequest.request(eq(MANAGER_ID), any())).willReturn(response);

			mockMvc.perform(post("/api/v1/attendance/{attendanceId}/correction-requests", ATTENDANCE_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.attendanceId").value(ATTENDANCE_ID))
				.andExpect(jsonPath("$.requestedStatus").value("PRESENT"));

			then(correctionRequest).should().request(eq(MANAGER_ID), any());
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void request_correction_without_token() throws Exception {
			CorrectionRequest request = new CorrectionRequest(AttendanceStatus.PRESENT, "지각이 아니라 출석입니다.");

			mockMvc.perform(post("/api/v1/attendance/{attendanceId}/correction-requests", ATTENDANCE_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(correctionRequest).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 수정 요청 권한 없음 - 403 Forbidden")
		void request_correction_without_permission() throws Exception {
			String nonManagerToken = "non-manager-token";
			JwtProvider.Claims nonManagerClaims = new JwtProvider.Claims(
				NON_MANAGER_ID,
				"user@example.com",
				Instant.now().plusSeconds(3600)
			);

			given(jwtProvider.isValid(nonManagerToken)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(nonManagerToken)).willReturn(nonManagerClaims);
			given(systemAuthorizationPort.getRoles(NON_MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));
			given(attendanceSecurity.canRequestCorrection(ATTENDANCE_ID, NON_MANAGER_ID)).willReturn(false);

			CorrectionRequest request = new CorrectionRequest(AttendanceStatus.PRESENT, "지각이 아니라 출석입니다.");

			mockMvc.perform(post("/api/v1/attendance/{attendanceId}/correction-requests", ATTENDANCE_ID)
					.header("Authorization", "Bearer " + nonManagerToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(correctionRequest).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("출석 수정 승인 API (PATCH /api/v1/attendance/{attendanceId}/correction-requests/approve)")
	class ApproveCorrectionTest {

		@Test
		@DisplayName("[Success] 출석 수정 승인 성공 - 200 OK")
		void approve_correction_success() throws Exception {
			AttendanceCorrectionApproval.Response response = new AttendanceCorrectionApproval.Response(
				ATTENDANCE_ID,
				AttendanceStatus.LATE,
				AttendanceStatus.PRESENT,
				MANAGER_ID,
				Instant.now()
			);

			given(correctionApproval.approve(eq(MANAGER_ID), any())).willReturn(response);

			mockMvc.perform(patch("/api/v1/attendance/{attendanceId}/correction-requests/approve", ATTENDANCE_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.attendanceId").value(ATTENDANCE_ID))
				.andExpect(jsonPath("$.newStatus").value("PRESENT"));

			then(correctionApproval).should().approve(eq(MANAGER_ID), any());
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void approve_correction_without_token() throws Exception {
			mockMvc.perform(patch("/api/v1/attendance/{attendanceId}/correction-requests/approve", ATTENDANCE_ID))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(correctionApproval).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 승인 권한 없음 - 403 Forbidden")
		void approve_correction_without_permission() throws Exception {
			String nonManagerToken = "non-manager-token";
			JwtProvider.Claims nonManagerClaims = new JwtProvider.Claims(
				NON_MANAGER_ID,
				"user@example.com",
				Instant.now().plusSeconds(3600)
			);

			given(jwtProvider.isValid(nonManagerToken)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(nonManagerToken)).willReturn(nonManagerClaims);
			given(systemAuthorizationPort.getRoles(NON_MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));
			given(attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, NON_MANAGER_ID)).willReturn(false);

			mockMvc.perform(patch("/api/v1/attendance/{attendanceId}/correction-requests/approve", ATTENDANCE_ID)
					.header("Authorization", "Bearer " + nonManagerToken))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(correctionApproval).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("출석 수정 거절 API (PATCH /api/v1/attendance/{attendanceId}/correction-requests/reject)")
	class RejectCorrectionTest {

		@Test
		@DisplayName("[Success] 출석 수정 거절 성공 - 200 OK")
		void reject_correction_success() throws Exception {
			AttendanceRejectionRequest request = new AttendanceRejectionRequest("증빙 자료가 불충분합니다.");

			AttendanceCorrectionRejection.Response response = new AttendanceCorrectionRejection.Response(
				ATTENDANCE_ID,
				MANAGER_ID,
				"증빙 자료가 불충분합니다.",
				Instant.now()
			);

			given(correctionRejection.reject(eq(MANAGER_ID), any())).willReturn(response);

			mockMvc.perform(patch("/api/v1/attendance/{attendanceId}/correction-requests/reject", ATTENDANCE_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.attendanceId").value(ATTENDANCE_ID))
				.andExpect(jsonPath("$.rejectionReason").value("증빙 자료가 불충분합니다."));

			then(correctionRejection).should().reject(eq(MANAGER_ID), any());
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void reject_correction_without_token() throws Exception {
			AttendanceRejectionRequest request = new AttendanceRejectionRequest("증빙 자료가 불충분합니다.");

			mockMvc.perform(patch("/api/v1/attendance/{attendanceId}/correction-requests/reject", ATTENDANCE_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(correctionRejection).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 승인 권한 없음 (거절에도 동일 권한 필요) - 403 Forbidden")
		void reject_correction_without_permission() throws Exception {
			String nonManagerToken = "non-manager-token";
			JwtProvider.Claims nonManagerClaims = new JwtProvider.Claims(
				NON_MANAGER_ID,
				"user@example.com",
				Instant.now().plusSeconds(3600)
			);

			given(jwtProvider.isValid(nonManagerToken)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(nonManagerToken)).willReturn(nonManagerClaims);
			given(systemAuthorizationPort.getRoles(NON_MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));
			given(attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, NON_MANAGER_ID)).willReturn(false);

			AttendanceRejectionRequest request = new AttendanceRejectionRequest("증빙 자료가 불충분합니다.");

			mockMvc.perform(patch("/api/v1/attendance/{attendanceId}/correction-requests/reject", ATTENDANCE_ID)
					.header("Authorization", "Bearer " + nonManagerToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(correctionRejection).shouldHaveNoInteractions();
		}
	}
}

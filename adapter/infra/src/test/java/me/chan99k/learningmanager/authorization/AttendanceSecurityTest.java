package me.chan99k.learningmanager.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.attendance.Attendance;
import me.chan99k.learningmanager.attendance.AttendanceQueryRepository;
import me.chan99k.learningmanager.attendance.CorrectionRequested;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceSecurity 테스트")
class AttendanceSecurityTest {

	private static final String ATTENDANCE_ID = "attendance-1";
	private static final Long MEMBER_ID = 100L;
	private static final Long SESSION_ID = 1L;
	private static final Long COURSE_ID = 10L;
	private static final Long REQUESTER_ID = 200L;

	@InjectMocks
	private AttendanceSecurity attendanceSecurity;

	@Mock
	private AttendanceQueryRepository attendanceQueryRepository;

	@Mock
	private SessionQueryRepository sessionQueryRepository;

	@Mock
	private SystemAuthorizationPort systemAuthorizationPort;

	@Mock
	private CourseAuthorizationPort courseAuthorizationPort;

	@Mock
	private Attendance attendance;

	@Mock
	private Session session;

	@Mock
	private CorrectionRequested pendingRequest;

	// ========== 헬퍼 메서드 ==========

	static Stream<Arguments> allSecurityMethods() {
		return Stream.of(
			Arguments.of("canRequestCorrection",
				(BiFunction<AttendanceSecurity, Object[], Boolean>)(svc, args) ->
					svc.canRequestCorrection((String)args[0], (Long)args[1])),
			Arguments.of("canApproveCorrection",
				(BiFunction<AttendanceSecurity, Object[], Boolean>)(svc, args) ->
					svc.canApproveCorrection((String)args[0], (Long)args[1]))
		);
	}

	// 출석 정보 존재 설정
	private void givenAttendanceExists() {
		when(attendanceQueryRepository.findById(ATTENDANCE_ID)).thenReturn(Optional.of(attendance));
	}

	// 시스템 권한(ADMIN/REGISTRAR) 보유 설정
	private void givenHasSystemRole(boolean hasRole) {
		when(systemAuthorizationPort.hasAnyRole(MEMBER_ID, Set.of(SystemRole.ADMIN, SystemRole.REGISTRAR)))
			.thenReturn(hasRole);
	}

	// 출석의 세션 ID 설정
	private void givenAttendanceSessionId() {
		when(attendance.getSessionId()).thenReturn(SESSION_ID);
	}

	// 세션 정보 존재 설정
	private void givenSessionExists() {
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
	}

	// 과정 세션 설정 (courseId 존재)
	private void givenCourseSession() {
		when(session.getCourseId()).thenReturn(COURSE_ID);
	}

	// 독립 세션 설정 (courseId가 null)
	private void givenStandaloneSession() {
		when(session.getCourseId()).thenReturn(null);
	}

	// 대기중인 수정 요청 존재 설정
	private void givenPendingRequestExists() {
		when(attendance.getPendingRequest()).thenReturn(pendingRequest);
	}

	// 출석 존재 + 시스템 권한 없음 + 세션 조회 기본 설정
	private void givenBasicSetupWithoutSystemRole() {
		givenAttendanceExists();
		givenHasSystemRole(false);
		givenAttendanceSessionId();
	}

	// ========== 공통 패턴을 위한 ParameterizedTest ==========

	// 과정 세션까지 포함한 전체 기본 설정
	private void givenFullCourseSessionSetup() {
		givenBasicSetupWithoutSystemRole();
		givenSessionExists();
		givenCourseSession();
	}

	@ParameterizedTest(name = "{0}: 출석 정보가 없으면 false를 반환한다")
	@MethodSource("allSecurityMethods")
	@DisplayName("[Failure] 출석 정보가 없으면 false를 반환한다")
	void test01(String methodName, BiFunction<AttendanceSecurity, Object[], Boolean> method) {
		when(attendanceQueryRepository.findById(ATTENDANCE_ID)).thenReturn(Optional.empty());

		boolean result = method.apply(attendanceSecurity, new Object[] {ATTENDANCE_ID, MEMBER_ID});

		assertThat(result).isFalse();
	}

	@ParameterizedTest(name = "{0}: ADMIN/REGISTRAR 권한이 있으면 true를 반환한다")
	@MethodSource("allSecurityMethods")
	@DisplayName("[Success] ADMIN/REGISTRAR 권한이 있으면 true를 반환한다")
	void test02(String methodName, BiFunction<AttendanceSecurity, Object[], Boolean> method) {
		givenAttendanceExists();
		lenient().when(attendance.getPendingRequest()).thenReturn(pendingRequest);
		givenHasSystemRole(true);

		boolean result = method.apply(attendanceSecurity, new Object[] {ATTENDANCE_ID, MEMBER_ID});

		assertThat(result).isTrue();
	}

	@ParameterizedTest(name = "{0}: 세션 정보가 없으면 false를 반환한다")
	@MethodSource("allSecurityMethods")
	@DisplayName("[Failure] 세션 정보가 없으면 false를 반환한다")
	void test03(String methodName, BiFunction<AttendanceSecurity, Object[], Boolean> method) {
		givenBasicSetupWithoutSystemRole();
		lenient().when(attendance.getPendingRequest()).thenReturn(pendingRequest);
		when(sessionQueryRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		boolean result = method.apply(attendanceSecurity, new Object[] {ATTENDANCE_ID, MEMBER_ID});

		assertThat(result).isFalse();
	}

	@ParameterizedTest(name = "{0}: 독립 세션이면 false를 반환한다")
	@MethodSource("allSecurityMethods")
	@DisplayName("[Failure] 독립 세션(courseId가 null)이면 false를 반환한다")
	void test04(String methodName, BiFunction<AttendanceSecurity, Object[], Boolean> method) {
		givenBasicSetupWithoutSystemRole();
		lenient().when(attendance.getPendingRequest()).thenReturn(pendingRequest);
		givenSessionExists();
		givenStandaloneSession();

		boolean result = method.apply(attendanceSecurity, new Object[] {ATTENDANCE_ID, MEMBER_ID});

		assertThat(result).isFalse();
	}

	// ========== canRequestCorrection 개별 테스트 ==========

	@Nested
	@DisplayName("canRequestCorrection - 과정 세션")
	class CanRequestCorrectionCourseSessionTest {

		@Test
		@DisplayName("[Success] MENTOR/MANAGER/LEAD_MANAGER 권한이 있으면 true를 반환한다")
		void test01() {
			givenFullCourseSessionSetup();
			when(courseAuthorizationPort.hasAnyRole(
				MEMBER_ID, COURSE_ID, List.of(CourseRole.MENTOR, CourseRole.MANAGER, CourseRole.LEAD_MANAGER)
			)).thenReturn(true);

			boolean result = attendanceSecurity.canRequestCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] CourseRole이 없으면 false를 반환한다")
		void test02() {
			givenFullCourseSessionSetup();
			when(courseAuthorizationPort.hasAnyRole(
				MEMBER_ID, COURSE_ID, List.of(CourseRole.MENTOR, CourseRole.MANAGER, CourseRole.LEAD_MANAGER)
			)).thenReturn(false);

			boolean result = attendanceSecurity.canRequestCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}
	}

	// ========== canApproveCorrection 개별 테스트 ==========

	@Nested
	@DisplayName("canApproveCorrection - 대기 요청 검증")
	class CanApproveCorrectionPendingRequestTest {

		@Test
		@DisplayName("[Failure] 대기중인 수정 요청이 없으면 false를 반환한다")
		void test01() {
			givenAttendanceExists();
			when(attendance.getPendingRequest()).thenThrow(new IllegalStateException("No pending request"));

			boolean result = attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("canApproveCorrection - 과정 세션")
	class CanApproveCorrectionCourseSessionTest {

		@Test
		@DisplayName("[Success] LEAD_MANAGER 권한이 있으면 true를 반환한다")
		void test01() {
			givenFullCourseSessionSetup();
			givenPendingRequestExists();
			when(courseAuthorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.LEAD_MANAGER)).thenReturn(true);

			boolean result = attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 본인의 수정 요청을 본인이 승인하려 하면 false를 반환한다")
		void test02() {
			givenFullCourseSessionSetup();
			givenPendingRequestExists();
			when(courseAuthorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.LEAD_MANAGER)).thenReturn(false);
			when(pendingRequest.requestedBy()).thenReturn(MEMBER_ID);

			boolean result = attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Success] MENTOR의 요청을 MANAGER가 승인할 수 있다")
		void test03() {
			givenFullCourseSessionSetup();
			givenPendingRequestExists();
			when(courseAuthorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.LEAD_MANAGER)).thenReturn(false);
			when(pendingRequest.requestedBy()).thenReturn(REQUESTER_ID);
			// 요청자가 MENTOR
			when(courseAuthorizationPort.hasRole(REQUESTER_ID, COURSE_ID, CourseRole.MENTOR)).thenReturn(true);
			// 승인자가 MANAGER 또는 LEAD_MANAGER
			when(courseAuthorizationPort.hasAnyRole(
				MEMBER_ID, COURSE_ID, List.of(CourseRole.MANAGER, CourseRole.LEAD_MANAGER)
			)).thenReturn(true);

			boolean result = attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Success] MANAGER의 요청을 LEAD_MANAGER가 승인할 수 있다")
		void test04() {
			givenFullCourseSessionSetup();
			givenPendingRequestExists();
			// 첫 번째 호출(isLeadManager)에서 false, 두 번째 호출(hasHigherCourseRoleThan)에서 true 반환
			when(courseAuthorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.LEAD_MANAGER))
				.thenReturn(false, true);
			when(pendingRequest.requestedBy()).thenReturn(REQUESTER_ID);
			// 요청자가 MENTOR 아님
			when(courseAuthorizationPort.hasRole(REQUESTER_ID, COURSE_ID, CourseRole.MENTOR)).thenReturn(false);
			// 요청자가 MANAGER
			when(courseAuthorizationPort.hasRole(REQUESTER_ID, COURSE_ID, CourseRole.MANAGER)).thenReturn(true);

			boolean result = attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 동등 또는 하위 권한자의 승인 시도는 false를 반환한다")
		void test05() {
			givenFullCourseSessionSetup();
			givenPendingRequestExists();
			when(courseAuthorizationPort.hasRole(MEMBER_ID, COURSE_ID, CourseRole.LEAD_MANAGER)).thenReturn(false);
			when(pendingRequest.requestedBy()).thenReturn(REQUESTER_ID);
			// 요청자가 MENTOR도 MANAGER도 아님
			when(courseAuthorizationPort.hasRole(REQUESTER_ID, COURSE_ID, CourseRole.MENTOR)).thenReturn(false);
			when(courseAuthorizationPort.hasRole(REQUESTER_ID, COURSE_ID, CourseRole.MANAGER)).thenReturn(false);

			boolean result = attendanceSecurity.canApproveCorrection(ATTENDANCE_ID, MEMBER_ID);

			assertThat(result).isFalse();
		}
	}
}

package me.chan99k.learningmanager.application.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.application.attendance.provides.AttendanceRetrieval;
import me.chan99k.learningmanager.application.session.SessionQueryRepository;
import me.chan99k.learningmanager.application.session.dto.SessionInfo;
import me.chan99k.learningmanager.domain.attendance.AttendanceStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceRetrievalService 테스트")
class AttendanceRetrievalServiceTest {

	private static final Long MEMBER_ID = 123L;
	private static final Long COURSE_ID = 456L;
	private static final Long CURRICULUM_ID = 789L;
	private static final Long SESSION_ID_1 = 101L;
	private static final Long SESSION_ID_2 = 102L;
	@Mock
	private AttendanceQueryRepository attendanceQueryRepository;
	@Mock
	private SessionQueryRepository sessionQueryRepository;
	@InjectMocks
	private AttendanceRetrievalService attendanceRetrievalService;
	private List<AttendanceQueryRepository.AttendanceProjection> mockAttendances;
	private Map<Long, SessionInfo> mockSessionInfoMap;

	@BeforeEach
	void setUp() {
		// Mock 출석 데이터
		mockAttendances = List.of(
			new AttendanceQueryRepository.AttendanceProjection(
				"attendance1", SESSION_ID_1, MEMBER_ID, AttendanceStatus.PRESENT
			),
			new AttendanceQueryRepository.AttendanceProjection(
				"attendance2", SESSION_ID_2, MEMBER_ID, AttendanceStatus.ABSENT
			)
		);

		mockSessionInfoMap = Map.of(
			SESSION_ID_1, new SessionInfo(
				SESSION_ID_1, "스프링 부트 기초",
				Instant.parse("2025-01-15T10:00:00Z"),
				COURSE_ID, "백엔드 부트캠프",
				CURRICULUM_ID, "웹 개발 기초"
			),
			SESSION_ID_2, new SessionInfo(
				SESSION_ID_2, "JPA 심화",
				Instant.parse("2025-01-16T14:00:00Z"),
				COURSE_ID, "백엔드 부트캠프",
				CURRICULUM_ID, "웹 개발 기초"
			)
		);
	}

	@Test
	@DisplayName("전체 출석 현황 조회 - 성공")
	void getMyAllAttendanceStatus_Success() {
		// Given
		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);
		AttendanceRetrieval.AllAttendanceRequest request =
			new AttendanceRetrieval.AllAttendanceRequest(MEMBER_ID);

		when(sessionQueryRepository.findSessionIdsByMemberId(MEMBER_ID))
			.thenReturn(sessionIds);
		when(attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds))
			.thenReturn(mockAttendances);
		when(sessionQueryRepository.findSessionInfoMapByIds(any()))
			.thenReturn(mockSessionInfoMap);

		// When
		AttendanceRetrieval.Response response = attendanceRetrievalService.getMyAllAttendanceStatus(request);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.sessions()).hasSize(2);

		// 첫 번째 세션 검증
		AttendanceRetrieval.SessionAttendanceInfo firstSession = response.sessions().get(0);
		assertThat(firstSession.attendanceId()).isEqualTo("attendance1");
		assertThat(firstSession.sessionId()).isEqualTo(SESSION_ID_1);
		assertThat(firstSession.sessionTitle()).isEqualTo("스프링 부트 기초");
		assertThat(firstSession.finalStatus()).isEqualTo(AttendanceStatus.PRESENT);

		// 통계 검증
		AttendanceRetrieval.AttendanceStatistics stats = response.statistics();
		assertThat(stats.totalSessions()).isEqualTo(2);
		assertThat(stats.presentCount()).isEqualTo(1);
		assertThat(stats.absentCount()).isEqualTo(1);
		assertThat(stats.attendanceRate()).isEqualTo(50.0);

		// Mock 호출 검증
		verify(sessionQueryRepository).findSessionIdsByMemberId(MEMBER_ID);
		verify(attendanceQueryRepository).findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds);
	}

	@Test
	@DisplayName("과정별 출석 현황 조회 - 성공")
	void getMyCourseAttendanceStatus_Success() {
		// Given
		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);
		AttendanceRetrieval.CourseAttendanceRequest request =
			new AttendanceRetrieval.CourseAttendanceRequest(MEMBER_ID, COURSE_ID);

		when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
			.thenReturn(sessionIds);
		when(attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds))
			.thenReturn(mockAttendances);
		when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
			.thenReturn(mockSessionInfoMap);

		// When
		AttendanceRetrieval.Response response = attendanceRetrievalService.getMyCourseAttendanceStatus(request);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.sessions()).hasSize(2);

		// 첫 번째 세션 검증
		AttendanceRetrieval.SessionAttendanceInfo firstSession = response.sessions().get(0);
		assertThat(firstSession.attendanceId()).isEqualTo("attendance1");
		assertThat(firstSession.sessionId()).isEqualTo(SESSION_ID_1);
		assertThat(firstSession.sessionTitle()).isEqualTo("스프링 부트 기초");
		assertThat(firstSession.finalStatus()).isEqualTo(AttendanceStatus.PRESENT);
		assertThat(firstSession.courseTitle()).isEqualTo("백엔드 부트캠프");

		// 통계 검증
		AttendanceRetrieval.AttendanceStatistics stats = response.statistics();
		assertThat(stats.totalSessions()).isEqualTo(2);
		assertThat(stats.presentCount()).isEqualTo(1);
		assertThat(stats.absentCount()).isEqualTo(1);
		assertThat(stats.attendanceRate()).isEqualTo(50.0);

		// Mock 호출 검증
		verify(sessionQueryRepository).findSessionIdsByCourseId(COURSE_ID);
		verify(attendanceQueryRepository).findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds);
		verify(sessionQueryRepository).findSessionInfoMapByIds(sessionIds);
	}

	@Test
	@DisplayName("커리큘럼별 출석 현황 조회 - 성공")
	void getMyCurriculumAttendanceStatus_Success() {
		// Given
		List<Long> sessionIds = List.of(SESSION_ID_1);
		AttendanceRetrieval.CurriculumAttendanceRequest request =
			new AttendanceRetrieval.CurriculumAttendanceRequest(MEMBER_ID, CURRICULUM_ID);

		List<AttendanceQueryRepository.AttendanceProjection> singleAttendance = List.of(mockAttendances.get(0));
		Map<Long, SessionInfo> singleSessionInfo = Map.of(
			SESSION_ID_1, mockSessionInfoMap.get(SESSION_ID_1)
		);

		when(sessionQueryRepository.findSessionIdsByCurriculumId(CURRICULUM_ID))
			.thenReturn(sessionIds);
		when(attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds))
			.thenReturn(singleAttendance);
		when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
			.thenReturn(singleSessionInfo);

		// When
		AttendanceRetrieval.Response response = attendanceRetrievalService.getMyCurriculumAttendanceStatus(request);

		// Then
		assertThat(response.sessions()).hasSize(1);
		assertThat(response.statistics().totalSessions()).isEqualTo(1);
		assertThat(response.statistics().presentCount()).isEqualTo(1);
		assertThat(response.statistics().attendanceRate()).isEqualTo(100.0);

		verify(sessionQueryRepository).findSessionIdsByCurriculumId(CURRICULUM_ID);
	}

	@Test
	@DisplayName("월별 출석 현황 조회 - 성공")
	void getMyMonthlyAttendanceStatus_Success() {
		// Given
		int year = 2025;
		int month = 1;
		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);
		AttendanceRetrieval.MonthlyAttendanceRequest request =
			new AttendanceRetrieval.MonthlyAttendanceRequest(MEMBER_ID, year, month, COURSE_ID, CURRICULUM_ID);

		when(sessionQueryRepository.findSessionIdsByMonthAndFilters(year, month, COURSE_ID, CURRICULUM_ID))
			.thenReturn(sessionIds);
		when(attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds))
			.thenReturn(mockAttendances);
		when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
			.thenReturn(mockSessionInfoMap);

		// When
		AttendanceRetrieval.Response response = attendanceRetrievalService.getMyMonthlyAttendanceStatus(request);

		// Then
		assertThat(response.sessions()).hasSize(2);
		verify(sessionQueryRepository).findSessionIdsByMonthAndFilters(year, month, COURSE_ID, CURRICULUM_ID);
	}

	@Test
	@DisplayName("기간별 출석 현황 조회 - 상태 필터링 포함")
	void getMyPeriodAttendanceStatus_WithStatusFilter_Success() {
		// Given
		Instant startDate = Instant.parse("2025-01-01T00:00:00Z");
		Instant endDate = Instant.parse("2025-01-31T23:59:59Z");
		AttendanceStatus filterStatus = AttendanceStatus.PRESENT;

		AttendanceRetrieval.PeriodAttendanceRequest request =
			new AttendanceRetrieval.PeriodAttendanceRequest(
				MEMBER_ID, startDate, endDate, COURSE_ID, CURRICULUM_ID, filterStatus
			);

		List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);

		when(sessionQueryRepository.findSessionIdsByPeriodAndFilters(startDate, endDate, COURSE_ID, CURRICULUM_ID))
			.thenReturn(sessionIds);
		when(attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds))
			.thenReturn(mockAttendances);
		when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
			.thenReturn(mockSessionInfoMap);

		// When
		AttendanceRetrieval.Response response = attendanceRetrievalService.getMyPeriodAttendanceStatus(request);

		// Then
		// PRESENT 상태만 필터링되어 1개만 남아야 함
		assertThat(response.sessions()).hasSize(1);
		assertThat(response.sessions().get(0).finalStatus()).isEqualTo(AttendanceStatus.PRESENT);

		verify(sessionQueryRepository).findSessionIdsByPeriodAndFilters(startDate, endDate, COURSE_ID, CURRICULUM_ID);
	}

	@Test
	@DisplayName("세션 정보가 없는 경우 기본값 처리")
	void buildSessionAttendanceInfos_MissingSessionInfo_DefaultValues() {
		// Given
		List<Long> sessionIds = List.of(SESSION_ID_1);
		AttendanceRetrieval.CourseAttendanceRequest request =
			new AttendanceRetrieval.CourseAttendanceRequest(MEMBER_ID, COURSE_ID);

		List<AttendanceQueryRepository.AttendanceProjection> attendances = List.of(
			new AttendanceQueryRepository.AttendanceProjection(
				"attendance1", SESSION_ID_1, MEMBER_ID, AttendanceStatus.PRESENT
			)
		);

		when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
			.thenReturn(sessionIds);
		when(attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, sessionIds))
			.thenReturn(attendances);
		when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
			.thenReturn(Map.of()); // 빈 맵 반환

		// When
		AttendanceRetrieval.Response response = attendanceRetrievalService.getMyCourseAttendanceStatus(request);

		// Then
		assertThat(response.sessions()).hasSize(1);
		AttendanceRetrieval.SessionAttendanceInfo session = response.sessions().get(0);
		assertThat(session.sessionTitle()).isEqualTo("Unknown Session");
		assertThat(session.courseTitle()).isEqualTo("Unknown Course");
		assertThat(session.curriculumTitle()).isEqualTo("Unknown Curriculum");
	}

	@Test
	@DisplayName("빈 세션 ID 목록인 경우 빈 결과 반환")
	void getMyCourseAttendanceStatus_EmptySessionIds_EmptyResult() {
		// Given
		AttendanceRetrieval.CourseAttendanceRequest request =
			new AttendanceRetrieval.CourseAttendanceRequest(MEMBER_ID, COURSE_ID);

		when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
			.thenReturn(List.of()); // 빈 목록
		when(attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(MEMBER_ID, List.of()))
			.thenReturn(List.of());
		when(sessionQueryRepository.findSessionInfoMapByIds(List.of()))
			.thenReturn(Map.of());

		// When
		AttendanceRetrieval.Response response = attendanceRetrievalService.getMyCourseAttendanceStatus(request);

		// Then
		assertThat(response.sessions()).isEmpty();
		assertThat(response.statistics().totalSessions()).isZero();
		assertThat(response.statistics().attendanceRate()).isZero();
	}
}
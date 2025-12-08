package me.chan99k.learningmanager.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.course.CourseMemberInfo;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.SessionQueryRepository;
import me.chan99k.learningmanager.session.dto.SessionInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseAttendanceRetrievalService 테스트")
class CourseAttendanceRetrievalServiceTest {

	private static final Long COURSE_ID = 1L;
	private static final Long CURRICULUM_ID = 10L;
	private static final Long MEMBER_ID_1 = 100L;
	private static final Long MEMBER_ID_2 = 101L;
	private static final Long SESSION_ID_1 = 200L;
	private static final Long SESSION_ID_2 = 201L;
	private static final Long REQUESTED_BY = 999L;

	@Mock
	private AttendanceQueryRepository attendanceQueryRepository;
	@Mock
	private SessionQueryRepository sessionQueryRepository;
	@Mock
	private CourseQueryRepository courseQueryRepository;

	@InjectMocks
	private CourseAttendanceRetrievalService service;

	private List<CourseMemberInfo> mockCourseMembers;
	private PageResult<CourseMemberInfo> mockMemberPageResult;
	private Map<Long, SessionInfo> mockSessionInfoMap;

	@BeforeEach
	void setUp() {
		Instant joinedAt = Instant.parse("2025-01-01T00:00:00Z");
		mockCourseMembers = List.of(
			new CourseMemberInfo(MEMBER_ID_1, "홍길동", "hong@test.com", CourseRole.MENTEE, joinedAt),
			new CourseMemberInfo(MEMBER_ID_2, "김철수", "kim@test.com", CourseRole.MENTEE, joinedAt)
		);

		mockMemberPageResult = PageResult.of(mockCourseMembers, 0, Integer.MAX_VALUE, 2);

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

	@Nested
	@DisplayName("getAllMembersAttendance")
	class GetAllMembersAttendanceTest {

		@Test
		@DisplayName("성공 - 전체 멤버 출석 현황 조회")
		void success() {
			// Given
			List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);
			var request = new CourseAttendanceRetrieval.AllMembersRequest(
				COURSE_ID, null, null, null, null, null
			);

			List<AttendanceQueryRepository.MemberAttendanceResult> mockResults = List.of(
				new AttendanceQueryRepository.MemberAttendanceResult(
					MEMBER_ID_1,
					List.of(
						new AttendanceQueryRepository.AttendanceRecord("att1", SESSION_ID_1, AttendanceStatus.PRESENT),
						new AttendanceQueryRepository.AttendanceRecord("att2", SESSION_ID_2, AttendanceStatus.LATE)
					),
					new AttendanceQueryRepository.AttendanceStats(2, 1, 0, 1, 0, 50.0)
				),
				new AttendanceQueryRepository.MemberAttendanceResult(
					MEMBER_ID_2,
					List.of(
						new AttendanceQueryRepository.AttendanceRecord("att3", SESSION_ID_1, AttendanceStatus.PRESENT),
						new AttendanceQueryRepository.AttendanceRecord("att4", SESSION_ID_2, AttendanceStatus.PRESENT)
					),
					new AttendanceQueryRepository.AttendanceStats(2, 2, 0, 0, 0, 100.0)
				)
			);

			when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
				.thenReturn(sessionIds);
			when(courseQueryRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(PageRequest.class)))
				.thenReturn(mockMemberPageResult);
			when(attendanceQueryRepository.findAllMembersAttendanceWithStats(sessionIds,
				List.of(MEMBER_ID_1, MEMBER_ID_2)))
				.thenReturn(mockResults);
			when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
				.thenReturn(mockSessionInfoMap);

			// When
			var response = service.getAllMembersAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.members()).hasSize(2);

			// 첫 번째 멤버 검증
			var firstMember = response.members().get(0);
			assertThat(firstMember.memberId()).isEqualTo(MEMBER_ID_1);
			assertThat(firstMember.memberName()).isEqualTo("홍길동");
			assertThat(firstMember.sessions()).hasSize(2);
			assertThat(firstMember.statistics().attendanceRate()).isEqualTo(50.0);

			// 두 번째 멤버 검증
			var secondMember = response.members().get(1);
			assertThat(secondMember.memberId()).isEqualTo(MEMBER_ID_2);
			assertThat(secondMember.statistics().attendanceRate()).isEqualTo(100.0);

			// 과정 통계 검증
			var courseStats = response.courseStatistics();
			assertThat(courseStats.totalMembers()).isEqualTo(2);
			assertThat(courseStats.totalSessions()).isEqualTo(2);
			assertThat(courseStats.averageAttendanceRate()).isEqualTo(75.0);

			// Mock 호출 검증
			verify(sessionQueryRepository).findSessionIdsByCourseId(COURSE_ID);
			verify(courseQueryRepository).findCourseMembersByCourseId(eq(COURSE_ID), any(PageRequest.class));
			verify(attendanceQueryRepository).findAllMembersAttendanceWithStats(sessionIds,
				List.of(MEMBER_ID_1, MEMBER_ID_2));
		}

		@Test
		@DisplayName("세션이 없는 경우 빈 결과 반환")
		void emptyWhenNoSessions() {
			// Given
			var request = new CourseAttendanceRetrieval.AllMembersRequest(
				COURSE_ID, null, null, null, null, null
			);

			when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
				.thenReturn(List.of());

			// When
			var response = service.getAllMembersAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response.members()).isEmpty();
			assertThat(response.courseStatistics().totalMembers()).isZero();
			assertThat(response.courseStatistics().totalSessions()).isZero();

			verify(courseQueryRepository, never()).findCourseMembersByCourseId(any(), any());
			verify(attendanceQueryRepository, never()).findAllMembersAttendanceWithStats(any(), any());
		}

		@Test
		@DisplayName("멤버가 없는 경우 빈 결과 반환")
		void emptyWhenNoMembers() {
			// Given
			List<Long> sessionIds = List.of(SESSION_ID_1);
			var request = new CourseAttendanceRetrieval.AllMembersRequest(
				COURSE_ID, null, null, null, null, null
			);

			PageResult<CourseMemberInfo> emptyMemberResult = PageResult.of(List.of(), 0, Integer.MAX_VALUE, 0);

			when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
				.thenReturn(sessionIds);
			when(courseQueryRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(PageRequest.class)))
				.thenReturn(emptyMemberResult);

			// When
			var response = service.getAllMembersAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response.members()).isEmpty();
			assertThat(response.courseStatistics().totalMembers()).isZero();

			verify(attendanceQueryRepository, never()).findAllMembersAttendanceWithStats(any(), any());
		}

		@Test
		@DisplayName("커리큘럼 필터 적용")
		void withCurriculumFilter() {
			// Given
			List<Long> sessionIds = List.of(SESSION_ID_1);
			var request = new CourseAttendanceRetrieval.AllMembersRequest(
				COURSE_ID, CURRICULUM_ID, null, null, null, null
			);

			List<AttendanceQueryRepository.MemberAttendanceResult> mockResults = List.of(
				new AttendanceQueryRepository.MemberAttendanceResult(
					MEMBER_ID_1,
					List.of(
						new AttendanceQueryRepository.AttendanceRecord("att1", SESSION_ID_1, AttendanceStatus.PRESENT)),
					new AttendanceQueryRepository.AttendanceStats(1, 1, 0, 0, 0, 100.0)
				)
			);

			when(sessionQueryRepository.findSessionIdsByCurriculumId(CURRICULUM_ID))
				.thenReturn(sessionIds);
			when(courseQueryRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(PageRequest.class)))
				.thenReturn(mockMemberPageResult);
			when(attendanceQueryRepository.findAllMembersAttendanceWithStats(any(), any()))
				.thenReturn(mockResults);
			when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
				.thenReturn(Map.of(SESSION_ID_1, mockSessionInfoMap.get(SESSION_ID_1)));

			// When
			var response = service.getAllMembersAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response.members()).hasSize(1);
			verify(sessionQueryRepository).findSessionIdsByCurriculumId(CURRICULUM_ID);
			verify(sessionQueryRepository, never()).findSessionIdsByCourseId(any());
		}

		@Test
		@DisplayName("월별 필터 적용")
		void withMonthlyFilter() {
			// Given
			int year = 2025;
			int month = 1;
			List<Long> sessionIds = List.of(SESSION_ID_1);
			var request = new CourseAttendanceRetrieval.AllMembersRequest(
				COURSE_ID, null, year, month, null, null
			);

			List<AttendanceQueryRepository.MemberAttendanceResult> mockResults = List.of(
				new AttendanceQueryRepository.MemberAttendanceResult(
					MEMBER_ID_1,
					List.of(
						new AttendanceQueryRepository.AttendanceRecord("att1", SESSION_ID_1, AttendanceStatus.PRESENT)),
					new AttendanceQueryRepository.AttendanceStats(1, 1, 0, 0, 0, 100.0)
				)
			);

			when(sessionQueryRepository.findSessionIdsByMonthAndFilters(year, month, COURSE_ID, null))
				.thenReturn(sessionIds);
			when(courseQueryRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(PageRequest.class)))
				.thenReturn(mockMemberPageResult);
			when(attendanceQueryRepository.findAllMembersAttendanceWithStats(any(), any()))
				.thenReturn(mockResults);
			when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
				.thenReturn(Map.of(SESSION_ID_1, mockSessionInfoMap.get(SESSION_ID_1)));

			// When
			var response = service.getAllMembersAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response.members()).hasSize(1);
			verify(sessionQueryRepository).findSessionIdsByMonthAndFilters(year, month, COURSE_ID, null);
		}
	}

	@Nested
	@DisplayName("getMemberAttendance")
	class GetMemberAttendanceTest {

		@Test
		@DisplayName("성공 - 특정 멤버 출석 현황 조회")
		void success() {
			// Given
			List<Long> sessionIds = List.of(SESSION_ID_1, SESSION_ID_2);
			var request = new CourseAttendanceRetrieval.MemberRequest(
				COURSE_ID, MEMBER_ID_1, null, null, null, null, null
			);

			var mockResult = new AttendanceQueryRepository.MemberAttendanceResult(
				MEMBER_ID_1,
				List.of(
					new AttendanceQueryRepository.AttendanceRecord("att1", SESSION_ID_1, AttendanceStatus.PRESENT),
					new AttendanceQueryRepository.AttendanceRecord("att2", SESSION_ID_2, AttendanceStatus.ABSENT)
				),
				new AttendanceQueryRepository.AttendanceStats(2, 1, 1, 0, 0, 50.0)
			);

			when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
				.thenReturn(sessionIds);
			when(attendanceQueryRepository.findMemberAttendanceWithStats(MEMBER_ID_1, sessionIds))
				.thenReturn(mockResult);
			when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
				.thenReturn(mockSessionInfoMap);
			when(courseQueryRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(PageRequest.class)))
				.thenReturn(mockMemberPageResult);

			// When
			var response = service.getMemberAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response.members()).hasSize(1);

			var member = response.members().get(0);
			assertThat(member.memberId()).isEqualTo(MEMBER_ID_1);
			assertThat(member.memberName()).isEqualTo("홍길동");
			assertThat(member.sessions()).hasSize(2);
			assertThat(member.statistics().totalSessions()).isEqualTo(2);
			assertThat(member.statistics().presentCount()).isEqualTo(1);
			assertThat(member.statistics().absentCount()).isEqualTo(1);
			assertThat(member.statistics().attendanceRate()).isEqualTo(50.0);

			// 과정 통계 (단일 멤버)
			assertThat(response.courseStatistics().totalMembers()).isEqualTo(1);

			verify(attendanceQueryRepository).findMemberAttendanceWithStats(MEMBER_ID_1, sessionIds);
		}

		@Test
		@DisplayName("세션이 없는 경우 빈 결과 반환")
		void emptyWhenNoSessions() {
			// Given
			var request = new CourseAttendanceRetrieval.MemberRequest(
				COURSE_ID, MEMBER_ID_1, null, null, null, null, null
			);

			when(sessionQueryRepository.findSessionIdsByCourseId(COURSE_ID))
				.thenReturn(List.of());

			// When
			var response = service.getMemberAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response.members()).isEmpty();
			assertThat(response.courseStatistics().totalMembers()).isZero();

			verify(attendanceQueryRepository, never()).findMemberAttendanceWithStats(any(), any());
		}

		@Test
		@DisplayName("기간 필터 적용")
		void withPeriodFilter() {
			// Given
			Instant startDate = Instant.parse("2025-01-01T00:00:00Z");
			Instant endDate = Instant.parse("2025-01-31T23:59:59Z");
			List<Long> sessionIds = List.of(SESSION_ID_1);

			var request = new CourseAttendanceRetrieval.MemberRequest(
				COURSE_ID, MEMBER_ID_1, null, null, null, startDate, endDate
			);

			var mockResult = new AttendanceQueryRepository.MemberAttendanceResult(
				MEMBER_ID_1,
				List.of(new AttendanceQueryRepository.AttendanceRecord("att1", SESSION_ID_1, AttendanceStatus.PRESENT)),
				new AttendanceQueryRepository.AttendanceStats(1, 1, 0, 0, 0, 100.0)
			);

			when(sessionQueryRepository.findSessionIdsByPeriodAndFilters(startDate, endDate, COURSE_ID, null))
				.thenReturn(sessionIds);
			when(attendanceQueryRepository.findMemberAttendanceWithStats(MEMBER_ID_1, sessionIds))
				.thenReturn(mockResult);
			when(sessionQueryRepository.findSessionInfoMapByIds(sessionIds))
				.thenReturn(Map.of(SESSION_ID_1, mockSessionInfoMap.get(SESSION_ID_1)));
			when(courseQueryRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(PageRequest.class)))
				.thenReturn(mockMemberPageResult);

			// When
			var response = service.getMemberAttendance(REQUESTED_BY, request);

			// Then
			assertThat(response.members()).hasSize(1);
			verify(sessionQueryRepository).findSessionIdsByPeriodAndFilters(startDate, endDate, COURSE_ID, null);
		}
	}
}

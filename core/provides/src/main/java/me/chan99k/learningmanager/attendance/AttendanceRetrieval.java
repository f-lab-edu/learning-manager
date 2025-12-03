package me.chan99k.learningmanager.attendance;

import java.time.Instant;
import java.util.List;

/**
 * 일반 회원용 출석 현황 조회 유스케이스
 * 본인의 출석 현황만 조회 가능
 */
public interface AttendanceRetrieval {

	Response getMyAllAttendanceStatus(AllAttendanceRequest request);

	Response getMyCourseAttendanceStatus(CourseAttendanceRequest request);

	Response getMyCurriculumAttendanceStatus(CurriculumAttendanceRequest request);

	Response getMyMonthlyAttendanceStatus(MonthlyAttendanceRequest request);

	Response getMyPeriodAttendanceStatus(PeriodAttendanceRequest request);

	// === Request Records ===

	record AllAttendanceRequest(
		Long memberId
	) {
	}

	record CourseAttendanceRequest(
		Long memberId,
		Long courseId
	) {
	}

	record CurriculumAttendanceRequest(
		Long memberId,
		Long curriculumId
	) {
	}

	record MonthlyAttendanceRequest(
		Long memberId,
		int year,
		int month,
		Long courseId,
		Long curriculumId
	) {
	}

	record PeriodAttendanceRequest(
		Long memberId,
		Instant startDate,
		Instant endDate,
		Long courseId,
		Long curriculumId,
		AttendanceStatus status
	) {
	}

	// === Response Records ===

	record Response(
		List<SessionAttendanceInfo> sessions,
		AttendanceStatistics statistics
	) {
	}

	record SessionAttendanceInfo(
		String attendanceId,        // Attendance.id (MongoDB)
		Long sessionId,
		String sessionTitle,
		Instant scheduledAt,
		AttendanceStatus finalStatus,   // Attendance.finalStatus
		Long courseId,
		String courseTitle,
		Long curriculumId,
		String curriculumTitle
	) {
	}

	record AttendanceStatistics(
		int totalSessions,
		int presentCount,
		int absentCount,
		int lateCount,
		int leftEarlyCount,
		double attendanceRate
	) {
		public static AttendanceStatistics calculate(List<SessionAttendanceInfo> sessions) {
			int total = sessions.size();
			int present = (int)sessions.stream().filter(s -> s.finalStatus() == AttendanceStatus.PRESENT).count();
			int absent = (int)sessions.stream().filter(s -> s.finalStatus() == AttendanceStatus.ABSENT).count();
			int late = (int)sessions.stream().filter(s -> s.finalStatus() == AttendanceStatus.LATE).count();
			int leftEarly = (int)sessions.stream().filter(s -> s.finalStatus() == AttendanceStatus.LEFT_EARLY).count();
			double rate = total > 0 ? (double)present / total * 100.0 : 0.0;

			return new AttendanceStatistics(total, present, absent, late, leftEarly, rate);
		}
	}
}

package me.chan99k.learningmanager.attendance;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public interface CourseAttendanceRetrieval {

	Response getAllMembersAttendance(Long requestedBy, AllMembersRequest request);

	Response getMemberAttendance(Long requestedBy, MemberRequest request);

	// === Request Records ===

	record AllMembersRequest(
		Long courseId,            // required
		Long curriculumId,
		Integer year,
		Integer month,
		Instant startDate,
		Instant endDate
	) {
		public AllMembersRequest {
			Objects.requireNonNull(courseId, "[System] courseId 는 필수 입니다.");
		}
	}

	record MemberRequest(
		Long courseId,    // required
		Long memberId,    // required
		Long curriculumId,
		Integer year,
		Integer month,
		Instant startDate,
		Instant endDate
	) {
		public MemberRequest {
			Objects.requireNonNull(courseId, "[System] courseId 는 필수 입니다.");
			Objects.requireNonNull(memberId, "[System] memberId 는 필수 입니다.");
		}
	}

	// === Response Records ===

	record Response(
		List<MemberAttendanceSummary> members,
		CourseAttendanceStatistics courseStatistics
	) {
	}

	record MemberAttendanceSummary(
		Long memberId,
		String memberName,
		List<SessionAttendanceInfo> sessions,
		MemberStatistics statistics
	) {
	}

	record SessionAttendanceInfo(
		String attendanceId,
		Long sessionId,
		String sessionTitle,
		Instant scheduledAt,
		AttendanceStatus finalStatus,
		Long curriculumId,
		String curriculumTitle
	) {
	}

	record MemberStatistics(
		int totalSessions,
		int presentCount,
		int absentCount,
		int lateCount,
		int leftEarlyCount,
		double attendanceRate
	) {
	}

	record CourseAttendanceStatistics(
		int totalMembers,
		int totalSessions,
		double averageAttendanceRate
	) {
	}
}

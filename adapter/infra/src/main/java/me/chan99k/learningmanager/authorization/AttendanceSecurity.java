package me.chan99k.learningmanager.authorization;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.attendance.Attendance;
import me.chan99k.learningmanager.attendance.AttendanceQueryRepository;
import me.chan99k.learningmanager.attendance.CorrectionRequested;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@Service("attendanceSecurity")
public class AttendanceSecurity {

	private final AttendanceQueryRepository attendanceQueryRepository;
	private final SessionQueryRepository sessionQueryRepository;
	private final SystemAuthorizationPort systemAuthorizationPort;
	private final CourseAuthorizationPort courseAuthorizationPort;

	public AttendanceSecurity(
		AttendanceQueryRepository attendanceQueryRepository,
		SessionQueryRepository sessionQueryRepository,
		SystemAuthorizationPort systemAuthorizationPort,
		CourseAuthorizationPort courseAuthorizationPort
	) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.sessionQueryRepository = sessionQueryRepository;
		this.systemAuthorizationPort = systemAuthorizationPort;
		this.courseAuthorizationPort = courseAuthorizationPort;
	}

	public boolean canRequestCorrection(String attendanceId, Long memberId) {
		Optional<Attendance> foundAttendance = attendanceQueryRepository.findById(attendanceId);
		if (foundAttendance.isEmpty()) {
			return false;
		}

		// SystemRole 우선 확인
		if (systemAuthorizationPort.hasAnyRole(memberId, Set.of(SystemRole.ADMIN, SystemRole.REGISTRAR))) {
			return true;
		}

		var attendance = foundAttendance.get();
		Optional<Session> foundSession = sessionQueryRepository.findById(attendance.getSessionId());
		if (foundSession.isEmpty()) {
			return false;
		}

		var courseId = foundSession.get().getCourseId();
		if (courseId == null) {
			return false;
		}

		// CourseRole 확인
		return courseAuthorizationPort.hasAnyRole(
			memberId,
			courseId,
			List.of(CourseRole.MENTOR, CourseRole.MANAGER, CourseRole.LEAD_MANAGER)
		);
	}

	public boolean canApproveCorrection(String attendanceId, Long memberId) {
		Optional<Attendance> foundAttendance = attendanceQueryRepository.findById(attendanceId);
		if (foundAttendance.isEmpty()) {
			return false;
		}

		var attendance = foundAttendance.get();
		CorrectionRequested pending;
		try {
			pending = attendance.getPendingRequest();
		} catch (IllegalStateException e) {
			return false; // 대기중인 수정 요청 없음
		}

		// SystemRole 우선 확인
		if (systemAuthorizationPort.hasAnyRole(memberId, Set.of(SystemRole.ADMIN, SystemRole.REGISTRAR))) {
			return true;
		}

		Optional<Session> foundSession = sessionQueryRepository.findById(attendance.getSessionId());
		if (foundSession.isEmpty()) {
			return false;
		}

		var courseId = foundSession.get().getCourseId();
		if (courseId == null) {
			return false; // 독립 세션은 시스템 권한으로만 승인 가능 (위에서 이미 처리됨)
		}

		boolean isLeadManager = courseAuthorizationPort.hasRole(memberId, courseId, CourseRole.LEAD_MANAGER);
		if (isLeadManager) {
			return true; // 본인의 수정 요청 포함 모두 승인 가능
		}

		if (pending.requestedBy().equals(memberId)) {
			return false; // 본인의 수정 요청을 스스로 승인하는 것을 방지
		}

		return hasHigherCourseRoleThan(memberId, pending.requestedBy(), courseId);
	}

	private boolean hasHigherCourseRoleThan(Long approverId, Long requesterId, Long courseId) {
		boolean requesterIsMentor = courseAuthorizationPort
			.hasRole(requesterId, courseId, CourseRole.MENTOR);
		boolean requesterIsManager = courseAuthorizationPort
			.hasRole(requesterId, courseId, CourseRole.MANAGER);

		if (requesterIsMentor) {
			return courseAuthorizationPort.hasAnyRole(
				approverId, courseId,
				List.of(CourseRole.MANAGER, CourseRole.LEAD_MANAGER)
			);
		}

		if (requesterIsManager) {
			return courseAuthorizationPort.hasRole(
				approverId, courseId, CourseRole.LEAD_MANAGER
			);
		}

		return false;
	}
}

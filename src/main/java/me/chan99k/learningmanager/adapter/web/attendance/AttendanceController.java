package me.chan99k.learningmanager.adapter.web.attendance;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.attendance.provides.AttendanceRetrieval;
import me.chan99k.learningmanager.domain.attendance.AttendanceStatus;

@RestController
@RequestMapping("/api/v1/attendance/status")
public class AttendanceController {

	private final AttendanceRetrieval attendanceRetrieval;

	public AttendanceController(AttendanceRetrieval attendanceRetrieval) {
		this.attendanceRetrieval = attendanceRetrieval;
	}

	/**
	 * 내 전체 출석 현황 조회
	 */
	@GetMapping("/my")
	public ResponseEntity<AttendanceRetrieval.Response> getMyAllAttendanceStatus(
		@AuthenticationPrincipal Long memberId
	) {
		AttendanceRetrieval.AllAttendanceRequest request =
			new AttendanceRetrieval.AllAttendanceRequest(memberId);

		AttendanceRetrieval.Response response = attendanceRetrieval.getMyAllAttendanceStatus(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 특정 과정에서의 내 출석 현황 조회
	 */
	@GetMapping("/my/course")
	public ResponseEntity<AttendanceRetrieval.Response> getMyCourseAttendanceStatus(
		@AuthenticationPrincipal Long memberId,
		@RequestParam Long courseId
	) {
		AttendanceRetrieval.CourseAttendanceRequest request =
			new AttendanceRetrieval.CourseAttendanceRequest(memberId, courseId);

		AttendanceRetrieval.Response response = attendanceRetrieval.getMyCourseAttendanceStatus(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 특정 커리큘럼에서의 내 출석 현황 조회
	 */
	@GetMapping("/my/curriculum")
	public ResponseEntity<AttendanceRetrieval.Response> getMyCurriculumAttendanceStatus(
		@AuthenticationPrincipal Long memberId,
		@RequestParam Long curriculumId
	) {
		AttendanceRetrieval.CurriculumAttendanceRequest request =
			new AttendanceRetrieval.CurriculumAttendanceRequest(memberId, curriculumId);

		AttendanceRetrieval.Response response = attendanceRetrieval.getMyCurriculumAttendanceStatus(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 월별 출석 현황 조회
	 */
	@GetMapping("/my/monthly")
	public ResponseEntity<AttendanceRetrieval.Response> getMyMonthlyAttendanceStatus(
		@AuthenticationPrincipal Long memberId,
		@RequestParam int year,
		@RequestParam int month,
		@RequestParam(required = false) Long courseId,
		@RequestParam(required = false) Long curriculumId
	) {
		AttendanceRetrieval.MonthlyAttendanceRequest request =
			new AttendanceRetrieval.MonthlyAttendanceRequest(
				memberId, year, month, courseId, curriculumId
			);

		AttendanceRetrieval.Response response = attendanceRetrieval.getMyMonthlyAttendanceStatus(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 기간별 출석 현황 조회
	 */
	@GetMapping("/my/period")
	public ResponseEntity<AttendanceRetrieval.Response> getMyPeriodAttendanceStatus(
		@AuthenticationPrincipal Long memberId,
		@RequestParam Instant startDate,
		@RequestParam Instant endDate,
		@RequestParam(required = false) Long courseId,
		@RequestParam(required = false) Long curriculumId,
		@RequestParam(required = false) AttendanceStatus status
	) {
		AttendanceRetrieval.PeriodAttendanceRequest request =
			new AttendanceRetrieval.PeriodAttendanceRequest(
				memberId, startDate, endDate, courseId, curriculumId, status
			);

		AttendanceRetrieval.Response response = attendanceRetrieval.getMyPeriodAttendanceStatus(request);
		return ResponseEntity.ok(response);
	}

}

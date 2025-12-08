package me.chan99k.learningmanager.controller.attendance;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.chan99k.learningmanager.attendance.CourseAttendanceRetrieval;
import me.chan99k.learningmanager.security.CustomUserDetails;

@Tag(name = "Course Attendance", description = "과정 출석 현황 관리 API (매니저 전용)")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/attendance")
public class CourseAttendanceController {
	private final CourseAttendanceRetrieval courseAttendanceRetrieval;

	public CourseAttendanceController(CourseAttendanceRetrieval courseAttendanceRetrieval) {
		this.courseAttendanceRetrieval = courseAttendanceRetrieval;
	}

	@Operation(summary = "과정 전체 멤버 출석 현황 조회", description = "과정에 속한 모든 멤버의 출석 현황을 조회합니다.")
	@PreAuthorize("@courseSecurity.isManagerOrMentor(#courseId, #user.memberId)")
	@GetMapping
	public ResponseEntity<CourseAttendanceRetrieval.Response> getAllMembersAttendance(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long courseId,
		@RequestParam(required = false) Long curriculumId,
		@RequestParam(required = false) Integer year,
		@RequestParam(required = false) Integer month,
		@RequestParam(required = false) Instant startDate,
		@RequestParam(required = false) Instant endDate
	) {
		CourseAttendanceRetrieval.AllMembersRequest request =
			new CourseAttendanceRetrieval.AllMembersRequest(
				courseId, curriculumId, year, month, startDate, endDate
			);

		CourseAttendanceRetrieval.Response response =
			courseAttendanceRetrieval.getAllMembersAttendance(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "특정 멤버 출석 현황 조회", description = "과정에 속한 특정 멤버의 출석 현황을 조회합니다.")
	@PreAuthorize("@courseSecurity.isManagerOrMentor(#courseId, #user.memberId)")
	@GetMapping("/members/{memberId}")
	public ResponseEntity<CourseAttendanceRetrieval.Response> getMemberAttendance(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long courseId,
		@PathVariable Long memberId,
		@RequestParam(required = false) Long curriculumId,
		@RequestParam(required = false) Integer year,
		@RequestParam(required = false) Integer month,
		@RequestParam(required = false) Instant startDate,
		@RequestParam(required = false) Instant endDate
	) {
		CourseAttendanceRetrieval.MemberRequest request =
			new CourseAttendanceRetrieval.MemberRequest(
				courseId, memberId, curriculumId, year, month, startDate, endDate
			);

		CourseAttendanceRetrieval.Response response =
			courseAttendanceRetrieval.getMemberAttendance(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}
}

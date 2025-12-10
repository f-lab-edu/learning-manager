package me.chan99k.learningmanager.controller.attendance;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.chan99k.learningmanager.attendance.AttendanceCorrectionApproval;
import me.chan99k.learningmanager.attendance.AttendanceCorrectionRejection;
import me.chan99k.learningmanager.attendance.AttendanceCorrectionRequest;
import me.chan99k.learningmanager.controller.attendance.requests.AttendanceRejectionRequest;
import me.chan99k.learningmanager.controller.attendance.requests.CorrectionRequest;
import me.chan99k.learningmanager.security.CustomUserDetails;

@Tag(name = "Attendance Correction", description = "출석 수정 요청/승인/거절 API")
@RestController
@RequestMapping("/api/v1/attendance/{attendanceId}/correction-requests")
public class AttendanceCorrectionController {

	private final AttendanceCorrectionRequest correctionRequest;
	private final AttendanceCorrectionApproval correctionApproval;
	private final AttendanceCorrectionRejection correctionRejection;

	public AttendanceCorrectionController(
		AttendanceCorrectionRequest correctionRequest,
		AttendanceCorrectionApproval correctionApproval,
		AttendanceCorrectionRejection correctionRejection
	) {
		this.correctionRequest = correctionRequest;
		this.correctionApproval = correctionApproval;
		this.correctionRejection = correctionRejection;
	}

	@PreAuthorize("@attendanceSecurity.canApproveCorrection(#attendanceId, #user.memberId)")
	@Operation(summary = "출석 수정 요청", description = "출석 상태 수정을 요청합니다.")
	@PostMapping
	public ResponseEntity<AttendanceCorrectionRequest.Response> requestCorrection(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable String attendanceId,
		@RequestBody CorrectionRequest requestDto
	) {
		AttendanceCorrectionRequest.Request request = new AttendanceCorrectionRequest.Request(
			attendanceId,
			requestDto.requestedStatus(),
			requestDto.reason()
		);

		AttendanceCorrectionRequest.Response response =
			correctionRequest.request(user.getMemberId(), request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PreAuthorize("@attendanceSecurity.canApproveCorrection(#attendanceId, #user.memberId)")
	@Operation(summary = "출석 수정 승인", description = "출석 수정 요청을 승인합니다.")
	@PatchMapping("/approve")
	public ResponseEntity<AttendanceCorrectionApproval.Response> approveCorrection(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable String attendanceId
	) {
		AttendanceCorrectionApproval.Request request =
			new AttendanceCorrectionApproval.Request(attendanceId);

		AttendanceCorrectionApproval.Response response =
			correctionApproval.approve(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("@attendanceSecurity.canApproveCorrection(#attendanceId, #user.memberId)")
	@Operation(summary = "출석 수정 거절", description = "출석 수정 요청을 거절합니다.")
	@PatchMapping("/reject")
	public ResponseEntity<AttendanceCorrectionRejection.Response> rejectCorrection(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable String attendanceId,
		@RequestBody AttendanceRejectionRequest requestDto
	) {
		AttendanceCorrectionRejection.Request request =
			new AttendanceCorrectionRejection.Request(attendanceId, requestDto.rejectionReason());

		AttendanceCorrectionRejection.Response response =
			correctionRejection.reject(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}

}

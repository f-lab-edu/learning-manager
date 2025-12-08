package me.chan99k.learningmanager.controller.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.chan99k.learningmanager.attendance.AttendanceCheckIn;
import me.chan99k.learningmanager.attendance.AttendanceProblemCode;
import me.chan99k.learningmanager.attendance.QRCodeGenerator;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.security.CustomUserDetails;

@Tag(name = "Attendance", description = "출석 API")
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceCheckInController {

	private final AttendanceCheckIn attendanceCheckInService;
	private final QRCodeGenerator qrCodeGenerator;

	public AttendanceCheckInController(AttendanceCheckIn attendanceCheckInService,
		QRCodeGenerator qrCodeGenerator) {
		this.attendanceCheckInService = attendanceCheckInService;
		this.qrCodeGenerator = qrCodeGenerator;
	}

	@Operation(summary = "출석 체크인", description = "QR 코드 토큰을 사용하여 세션에 출석 체크인합니다.")
	@PostMapping("/check-in/{token}")
	public ResponseEntity<AttendanceCheckIn.Response> checkIn(
		@AuthenticationPrincipal CustomUserDetails user,
		@RequestBody AttendanceCheckIn.Request request,
		@PathVariable String token
	) {
		validateQrCode(request.sessionId(), token);

		var response = attendanceCheckInService.checkIn(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}

	private void validateQrCode(Long sessionId, String verificationToken) {
		if (!qrCodeGenerator.validateQrCode(verificationToken, sessionId)) {
			throw new DomainException(AttendanceProblemCode.INVALID_QR_TOKEN);
		}
	}

}

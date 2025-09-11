package me.chan99k.learningmanager.adapter.web.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.attendance.provides.AttendanceCheckIn;
import me.chan99k.learningmanager.application.attendance.requires.QRCodeGenerator;
import me.chan99k.learningmanager.common.exception.AuthenticationException;

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

	@PostMapping("/check-in/{token}")
	public ResponseEntity<AttendanceCheckIn.Response> checkIn(
		@RequestBody AttendanceCheckIn.Request request,
		@PathVariable String token
	) {
		validateQrCode(request.sessionId(), token);

		var response = attendanceCheckInService.checkIn(request);

		return ResponseEntity.ok(response);
	}

	private void validateQrCode(Long sessionId, String verificationToken) {
		if (!qrCodeGenerator.validateQrCode(verificationToken, sessionId)) {
			throw new AuthenticationException(AuthProblemCode.FAILED_TO_VALIDATE_TOKEN);
		}
	}

}

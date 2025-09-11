package me.chan99k.learningmanager.adapter.web.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.attendance.provides.AttendanceCheckOut;
import me.chan99k.learningmanager.application.attendance.requires.QRCodeGenerator;
import me.chan99k.learningmanager.common.exception.AuthenticationException;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceCheckOutController {

	private final AttendanceCheckOut attendanceCheckOutService;
	private final QRCodeGenerator qrCodeGenerator;

	public AttendanceCheckOutController(
		AttendanceCheckOut attendanceCheckOutService,
		QRCodeGenerator qrCodeGenerator
	) {
		this.attendanceCheckOutService = attendanceCheckOutService;
		this.qrCodeGenerator = qrCodeGenerator;
	}

	@PostMapping("/check-out/{token}")
	public ResponseEntity<AttendanceCheckOut.Response> checkOut(
		@RequestBody AttendanceCheckOut.Request request,
		@PathVariable String token
	) {
		validateQrCode(request.sessionId(), token);

		var response = attendanceCheckOutService.checkOut(request);

		return ResponseEntity.ok(response);
	}

	private void validateQrCode(Long sessionId, String verificationToken) {
		if (!qrCodeGenerator.validateQrCode(verificationToken, sessionId)) {
			throw new AuthenticationException(AuthProblemCode.FAILED_TO_VALIDATE_TOKEN);
		}
	}
}

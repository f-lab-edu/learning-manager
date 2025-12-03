package me.chan99k.learningmanager.controller.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.attendance.AttendanceCheckOut;
import me.chan99k.learningmanager.attendance.AttendanceProblemCode;
import me.chan99k.learningmanager.attendance.QRCodeGenerator;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.security.CustomUserDetails;

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
		@AuthenticationPrincipal CustomUserDetails user,
		@RequestBody AttendanceCheckOut.Request request,
		@PathVariable String token
	) {
		validateQrCode(request.sessionId(), token);

		var response = attendanceCheckOutService.checkOut(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}

	private void validateQrCode(Long sessionId, String verificationToken) {
		if (!qrCodeGenerator.validateQrCode(verificationToken, sessionId)) {
			throw new DomainException(AttendanceProblemCode.INVALID_QR_TOKEN);
		}
	}
}

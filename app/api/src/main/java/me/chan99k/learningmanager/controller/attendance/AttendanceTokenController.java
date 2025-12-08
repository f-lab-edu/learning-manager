package me.chan99k.learningmanager.controller.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.attendance.GenerateAttendanceToken;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/sessions")
public class AttendanceTokenController {
	private final GenerateAttendanceToken generateAttendanceToken;

	public AttendanceTokenController(GenerateAttendanceToken generateAttendanceToken) {
		this.generateAttendanceToken = generateAttendanceToken;
	}

	@PreAuthorize("@sessionSecurity.isSessionManagerOrMentor(#sessionId, #user.memberId)")
	@PostMapping("/{sessionId}/attendance-token")
	public ResponseEntity<GenerateAttendanceToken.Response> generateToken(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long sessionId
	) {
		var request = new GenerateAttendanceToken.Request(sessionId);
		var response = generateAttendanceToken.generate(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}
}

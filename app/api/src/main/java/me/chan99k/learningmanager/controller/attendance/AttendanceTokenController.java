package me.chan99k.learningmanager.controller.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.chan99k.learningmanager.attendance.GenerateAttendanceToken;
import me.chan99k.learningmanager.security.CustomUserDetails;

@Tag(name = "Attendance Token", description = "출석 인증 토큰 API")
@RestController
@RequestMapping("/api/v1/sessions")
public class AttendanceTokenController {
	private final GenerateAttendanceToken generateAttendanceToken;

	public AttendanceTokenController(GenerateAttendanceToken generateAttendanceToken) {
		this.generateAttendanceToken = generateAttendanceToken;
	}

	@Operation(
		summary = "출석 인증 토큰 생성",
		description = "세션 매니저 또는 멘토가 출석 체크를 위한 인증 토큰(QR 코드)을 생성합니다. "
			+ "생성된 토큰은 멘티들이 출석 체크인 시 사용합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "토큰 생성 성공"),
		@ApiResponse(responseCode = "403", description = "권한 없음 (매니저/멘토만 가능)"),
		@ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
	})
	@PreAuthorize("@sessionSecurity.isSessionManagerOrMentor(#sessionId, #user.memberId)")
	@PostMapping("/{sessionId}/attendance-token")
	public ResponseEntity<GenerateAttendanceToken.Response> generateToken(
		@AuthenticationPrincipal CustomUserDetails user,
		@Parameter(description = "세션 ID", required = true) @PathVariable Long sessionId
	) {
		var request = new GenerateAttendanceToken.Request(sessionId);
		var response = generateAttendanceToken.generate(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}
}

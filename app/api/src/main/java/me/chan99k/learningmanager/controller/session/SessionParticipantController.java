package me.chan99k.learningmanager.controller.session;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.security.CustomUserDetails;
import me.chan99k.learningmanager.session.SessionParticipantManagement;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionParticipantController {

	private final SessionParticipantManagement sessionParticipantManagement;

	public SessionParticipantController(SessionParticipantManagement sessionParticipantManagement) {
		this.sessionParticipantManagement = sessionParticipantManagement;
	}

	@PreAuthorize("@sessionSecurity.canManageSessionParticipants(#sessionId, #user.memberId)")
	@PostMapping("/{sessionId}/participants")
	public ResponseEntity<Void> addParticipant(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long sessionId,
		@Valid @RequestBody SessionParticipantManagement.AddParticipantRequest request
	) {
		sessionParticipantManagement.addParticipant(user.getMemberId(), sessionId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("@sessionSecurity.canManageSessionParticipants(#sessionId, #user.memberId)")
	@DeleteMapping("/{sessionId}/participants/{memberId}")
	public ResponseEntity<Void> removeParticipant(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long sessionId,
		@PathVariable Long memberId
	) {
		var serviceRequest = new SessionParticipantManagement.RemoveParticipantRequest(sessionId, memberId);
		sessionParticipantManagement.removeParticipant(user.getMemberId(), serviceRequest);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("@sessionSecurity.canManageSessionParticipants(#sessionId, #user.memberId)")
	@PutMapping("/{sessionId}/participants/{memberId}/role")
	public ResponseEntity<Void> changeParticipantRole(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long sessionId,
		@PathVariable Long memberId,
		@Valid @RequestBody SessionParticipantManagement.ChangeRoleDto request
	) {
		var serviceRequest = new SessionParticipantManagement.ChangeParticipantRoleRequest(
			sessionId, memberId, request.newRole()
		);
		sessionParticipantManagement.changeParticipantRole(user.getMemberId(), serviceRequest);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("@sessionSecurity.isSessionMember(#sessionId, #user.memberId)")
	@DeleteMapping("/{sessionId}/participants/me")
	public ResponseEntity<Void> leaveSession(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long sessionId) {
		var serviceRequest = new SessionParticipantManagement.LeaveSessionRequest(sessionId);
		sessionParticipantManagement.leaveSession(user.getMemberId(), serviceRequest);
		return ResponseEntity.noContent().build();
	}
}

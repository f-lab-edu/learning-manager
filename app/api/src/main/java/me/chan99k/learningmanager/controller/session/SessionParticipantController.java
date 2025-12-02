package me.chan99k.learningmanager.controller.session;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.session.SessionParticipantManagement;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionParticipantController {

	private final SessionParticipantManagement sessionParticipantManagement;

	public SessionParticipantController(SessionParticipantManagement sessionParticipantManagement) {
		this.sessionParticipantManagement = sessionParticipantManagement;
	}

	@PostMapping("/{sessionId}/participants")
	public ResponseEntity<Void> addParticipant(
		@PathVariable("sessionId") Long sessionId,
		@Valid @RequestBody SessionParticipantManagement.AddParticipantRequest request
	) {
		sessionParticipantManagement.addParticipant(sessionId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/{sessionId}/participants/{memberId}")
	public ResponseEntity<Void> removeParticipant(
		@PathVariable Long sessionId,
		@PathVariable Long memberId
	) {
		var serviceRequest = new SessionParticipantManagement.RemoveParticipantRequest(sessionId, memberId);
		sessionParticipantManagement.removeParticipant(serviceRequest);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{sessionId}/participants/{memberId}/role")
	public ResponseEntity<Void> changeParticipantRole(
		@PathVariable Long sessionId,
		@PathVariable Long memberId,
		@Valid @RequestBody SessionParticipantManagement.ChangeRoleDto request
	) {
		var serviceRequest = new SessionParticipantManagement.ChangeParticipantRoleRequest(
			sessionId, memberId, request.newRole()
		);
		sessionParticipantManagement.changeParticipantRole(serviceRequest);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{sessionId}/participants/me")
	public ResponseEntity<Void> leaveSession(@PathVariable Long sessionId) {
		var serviceRequest = new SessionParticipantManagement.LeaveSessionRequest(sessionId);
		sessionParticipantManagement.leaveSession(serviceRequest);
		return ResponseEntity.noContent().build();
	}
}

package me.chan99k.learningmanager.adapter.web.session;

import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.AsyncTaskExecutor;
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
import me.chan99k.learningmanager.application.session.SessionParticipantService;
import me.chan99k.learningmanager.application.session.provides.SessionParticipantManagement;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionParticipantController {

	private final SessionParticipantService sessionParticipantService;
	private final AsyncTaskExecutor sessionTaskExecutor;

	public SessionParticipantController(SessionParticipantService sessionParticipantService,
		AsyncTaskExecutor sessionTaskExecutor) {
		this.sessionParticipantService = sessionParticipantService;
		this.sessionTaskExecutor = sessionTaskExecutor;
	}

	@PostMapping("/{sessionId}/participants")
	public CompletableFuture<ResponseEntity<SessionParticipantManagement.SessionParticipantResponse>> addParticipant(
		@PathVariable("sessionId") Long sessionId,
		@Valid @RequestBody SessionParticipantManagement.AddParticipantRequest request
	) {
		return CompletableFuture.supplyAsync(() -> {
			var response = sessionParticipantService.addParticipant(sessionId, request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		}, sessionTaskExecutor);
	}

	@DeleteMapping("/{sessionId}/participants/{memberId}")
	public CompletableFuture<ResponseEntity<SessionParticipantManagement.SessionParticipantResponse>> removeParticipant(
		@PathVariable Long sessionId,
		@PathVariable Long memberId
	) {
		return CompletableFuture.supplyAsync(() -> {
			var serviceRequest = new SessionParticipantManagement.RemoveParticipantRequest(sessionId, memberId);
			var response = sessionParticipantService.removeParticipant(serviceRequest);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}

	@PutMapping("/{sessionId}/participants/{memberId}/role")
	public CompletableFuture<ResponseEntity<SessionParticipantManagement.SessionParticipantResponse>> changeParticipantRole(
		@PathVariable Long sessionId,
		@PathVariable Long memberId,
		@Valid @RequestBody SessionParticipantManagement.ChangeRoleDto request
	) {
		return CompletableFuture.supplyAsync(() -> {
			var serviceRequest = new SessionParticipantManagement.ChangeParticipantRoleRequest(
				sessionId, memberId, request.newRole()
			);
			var response = sessionParticipantService.changeParticipantRole(serviceRequest);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}
}

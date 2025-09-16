package me.chan99k.learningmanager.adapter.web.session;

import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.session.SessionCreationService;
import me.chan99k.learningmanager.application.session.provides.SessionCreation;
import me.chan99k.learningmanager.application.session.provides.SessionDeletion;
import me.chan99k.learningmanager.application.session.provides.SessionDetailRetrieval;
import me.chan99k.learningmanager.application.session.provides.SessionUpdate;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {
	private final SessionCreationService sessionCreationService;
	private final SessionUpdate sessionUpdate;
	private final SessionDeletion sessionDeletion;
	private final SessionQueryRepository sessionQueryRepository;
	private final TaskExecutor sessionTaskExecutor;

	public SessionController(SessionCreationService sessionCreationService,
		SessionUpdate sessionUpdate,
		SessionDeletion sessionDeletion,
		SessionQueryRepository sessionQueryRepository,
		TaskExecutor sessionTaskExecutor) {
		this.sessionCreationService = sessionCreationService;
		this.sessionUpdate = sessionUpdate;
		this.sessionDeletion = sessionDeletion;
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionTaskExecutor = sessionTaskExecutor;
	}

	@PostMapping
	public CompletableFuture<ResponseEntity<SessionCreation.Response>> createSession(
		@Valid @RequestBody SessionCreation.Request request
	) {
		return CompletableFuture.supplyAsync(() -> {
			Session session = sessionCreationService.createSession(request);
			SessionCreation.Response response = new SessionCreation.Response(
				session.getId(),
				session.getTitle(),
				session.getScheduledAt(),
				session.getScheduledEndAt(),
				session.getType(),
				session.getLocation(),
				session.getLocationDetails(),
				session.getCourseId(),
				session.getCurriculumId()
			);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		}, sessionTaskExecutor);
	}

	@GetMapping("/{id}")
	public CompletableFuture<ResponseEntity<SessionDetailRetrieval.Response>> getSession(
		@PathVariable Long id
	) {
		return CompletableFuture.supplyAsync(() -> {
			Session session = sessionQueryRepository.findById(id)
				.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

			SessionDetailRetrieval.Response response = new SessionDetailRetrieval.Response(
				session.getId(),
				session.getTitle(),
				session.getScheduledAt(),
				session.getScheduledEndAt(),
				session.getType(),
				session.getLocation(),
				session.getLocationDetails(),
				session.getCourseId(),
				session.getCurriculumId(),
				session.getParent() != null ? session.getParent().getId() : null,
				session.getChildren().size(),
				session.getParticipants().size()
			);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}

	@PutMapping("/{sessionId}")
	public ResponseEntity<Void> updateSession(
		@PathVariable Long sessionId,
		@Valid @RequestBody SessionUpdate.Request request) {
		sessionUpdate.updateSession(sessionId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{sessionId}")
	public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
		sessionDeletion.deleteSession(sessionId);
		return ResponseEntity.noContent().build();
	}
}
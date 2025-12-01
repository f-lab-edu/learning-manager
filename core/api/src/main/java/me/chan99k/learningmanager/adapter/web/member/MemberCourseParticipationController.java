package me.chan99k.learningmanager.adapter.web.member;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.member.MemberCourseParticipationService;
import me.chan99k.learningmanager.application.member.MemberCourseParticipationService.ParticipatingCoursesResponse;

@RestController
@RequestMapping("/members")
public class MemberCourseParticipationController {

	private final MemberCourseParticipationService memberCourseParticipationService;
	private final Executor memberTaskExecutor;

	public MemberCourseParticipationController(MemberCourseParticipationService memberCourseParticipationService,
		Executor memberTaskExecutor) {
		this.memberCourseParticipationService = memberCourseParticipationService;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@GetMapping("/{memberId}/courses")
	public CompletableFuture<ResponseEntity<ParticipatingCoursesResponse>> getParticipatingCourses(
		@PathVariable Long memberId
	) {
		return CompletableFuture.supplyAsync(() -> {
			ParticipatingCoursesResponse response = memberCourseParticipationService.getParticipatingCourses(memberId);
			return ResponseEntity.ok(response);
		}, memberTaskExecutor);

	}
}
package me.chan99k.learningmanager.controller.member;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.member.MemberCourseParticipation;

@RestController
@RequestMapping("/members")
public class MemberCourseParticipationController {

	private final MemberCourseParticipation memberCourseParticipation;
	private final Executor memberTaskExecutor;

	public MemberCourseParticipationController(MemberCourseParticipation memberCourseParticipation,
		Executor memberTaskExecutor) {
		this.memberCourseParticipation = memberCourseParticipation;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@GetMapping("/{memberId}/courses")
	public CompletableFuture<ResponseEntity<MemberCourseParticipation.Response>> getParticipatingCourses(
		@PathVariable Long memberId
	) {
		return CompletableFuture.supplyAsync(() -> {
			MemberCourseParticipation.Response response = memberCourseParticipation.getParticipatingCourses(memberId);
			return ResponseEntity.ok(response);
		}, memberTaskExecutor);

	}
}
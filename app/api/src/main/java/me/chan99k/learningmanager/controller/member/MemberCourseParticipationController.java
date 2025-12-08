package me.chan99k.learningmanager.controller.member;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.member.MemberCourseParticipation;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/members")
public class MemberCourseParticipationController {

	private final MemberCourseParticipation memberCourseParticipation;
	private final Executor memberTaskExecutor;

	public MemberCourseParticipationController(MemberCourseParticipation memberCourseParticipation,
		Executor memberTaskExecutor) {
		this.memberCourseParticipation = memberCourseParticipation;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@GetMapping("/me/courses")
	public CompletableFuture<ResponseEntity<MemberCourseParticipation.Response>> getMyParticipatingCourses(
		@AuthenticationPrincipal CustomUserDetails user
	) {
		return CompletableFuture.supplyAsync(() -> {
			MemberCourseParticipation.Response response = memberCourseParticipation.getParticipatingCourses(
				user.getMemberId());
			return ResponseEntity.ok(response);
		}, memberTaskExecutor);
	}
}
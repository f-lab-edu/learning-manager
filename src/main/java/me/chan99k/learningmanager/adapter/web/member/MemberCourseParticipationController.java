package me.chan99k.learningmanager.adapter.web.member;

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

	public MemberCourseParticipationController(MemberCourseParticipationService memberCourseParticipationService) {
		this.memberCourseParticipationService = memberCourseParticipationService;
	}

	@GetMapping("/{memberId}/courses")
	public ResponseEntity<ParticipatingCoursesResponse> getParticipatingCourses(
		@PathVariable Long memberId
	) {
		ParticipatingCoursesResponse response = memberCourseParticipationService.getParticipatingCourses(memberId);
		return ResponseEntity.ok(response);
	}
}
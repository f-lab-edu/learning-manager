package me.chan99k.learningmanager.adapter.web.course;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.course.CourseMemberService;
import me.chan99k.learningmanager.application.course.provides.CourseMemberAddition;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseMemberController {

	private final CourseMemberService courseMemberService;

	public CourseMemberController(CourseMemberService courseMemberService) {
		this.courseMemberService = courseMemberService;
	}

	@PostMapping("/{courseId}/members")
	public ResponseEntity<CourseMemberAddition.Response> addMembersToCourse(
		@PathVariable Long courseId,
		@Valid @RequestBody CourseMemberAddition.Request request
	) {
		if (request.members().size() == 1) {    // 단일 요청: 예외 발생 시 전역 핸들러가 처리
			CourseMemberAddition.MemberAdditionItem item = request.members().get(0);

			courseMemberService.addSingleMember(courseId, item);

			CourseMemberAddition.Response response = new CourseMemberAddition.Response(
				1, 1, 0,
				List.of(new CourseMemberAddition.MemberResult(item.email(), item.role(), "SUCCESS", "과정 멤버 추가 성공"))
			);

			return ResponseEntity.ok(response);
		} else {    // 벌크 요청: 207 Multi-Status
			CourseMemberAddition.Response response = courseMemberService.addMultipleMembers(courseId,
				request.members());

			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
		}
	}

	@DeleteMapping("/{courseId}/members/{memberId}")
	public ResponseEntity<Void> removeMemberFromCourse(
		@PathVariable Long courseId,
		@PathVariable Long memberId
	) {
		courseMemberService.removeMemberFromCourse(courseId, memberId);

		return ResponseEntity.noContent().build();
	}
}

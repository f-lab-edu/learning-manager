package me.chan99k.learningmanager.controller.course;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.course.CourseMemberAddition;
import me.chan99k.learningmanager.course.CourseMemberRemoval;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseMemberController {

	private final CourseMemberAddition courseMemberAddition;
	private final CourseMemberRemoval courseMemberRemoval;

	public CourseMemberController(CourseMemberAddition courseMemberAddition, CourseMemberRemoval courseMemberRemoval) {
		this.courseMemberAddition = courseMemberAddition;
		this.courseMemberRemoval = courseMemberRemoval;
	}

	@PostMapping("/{courseId}/members")
	public ResponseEntity<CourseMemberAddition.Response> addMembersToCourse(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long courseId,
		@Valid @RequestBody CourseMemberAddition.Request request
	) {
		Long requestedBy = user.getMemberId();
		if (request.members().size() == 1) {    // 단일 요청: 예외 발생 시 전역 핸들러가 처리
			CourseMemberAddition.MemberAdditionItem item = request.members().get(0);

			courseMemberAddition.addSingleMember(requestedBy, courseId, item);

			CourseMemberAddition.Response response = new CourseMemberAddition.Response(
				1, 1, 0,
				List.of(new CourseMemberAddition.MemberResult(item.email(), item.role(), "SUCCESS", "과정 멤버 추가 성공"))
			);

			return ResponseEntity.ok(response);
		} else {    // 벌크 요청: 207 Multi-Status
			CourseMemberAddition.Response response = courseMemberAddition.addMultipleMembers(requestedBy, courseId,
				request.members());

			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
		}
	}

	@DeleteMapping("/{courseId}/members/{memberId}")
	public ResponseEntity<Void> removeMemberFromCourse(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long courseId,
		@PathVariable Long memberId
	) {
		courseMemberRemoval.removeMemberFromCourse(user.getMemberId(), courseId, memberId);

		return ResponseEntity.noContent().build();
	}
}

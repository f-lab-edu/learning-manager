package me.chan99k.learningmanager.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.member.MemberStatus;
import me.chan99k.learningmanager.member.MemberStatusChange;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/admin/members")
public class MemberAdminController {

	private final MemberStatusChange memberStatusChangeService;

	public MemberAdminController(MemberStatusChange memberStatusChangeService) {
		this.memberStatusChangeService = memberStatusChangeService;
	}

	@PutMapping("/{memberId}/status")
	public ResponseEntity<Void> changeStatus(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long memberId,
		@Valid @RequestBody ChangeStatusRequest request
	) {
		MemberStatusChange.Request serviceRequest = new MemberStatusChange.Request(
			memberId,
			request.status()
		);

		memberStatusChangeService.changeStatus(user.getMemberId(), serviceRequest);

		return ResponseEntity.noContent().build();
	}

	public record ChangeStatusRequest(
		MemberStatus status
	) {
	}
}
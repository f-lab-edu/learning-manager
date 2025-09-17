package me.chan99k.learningmanager.adapter.web.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.member.provides.MemberStatusChange;
import me.chan99k.learningmanager.domain.member.MemberStatus;

@RestController
@RequestMapping("/api/v1/admin/members")
public class MemberAdminController {

	private final MemberStatusChange memberStatusChangeService;

	public MemberAdminController(MemberStatusChange memberStatusChangeService) {
		this.memberStatusChangeService = memberStatusChangeService;
	}

	@PutMapping("/{memberId}/status")
	public ResponseEntity<Void> changeStatus(
		@PathVariable Long memberId,
		@Valid @RequestBody ChangeStatusRequest request
	) {
		MemberStatusChange.Request serviceRequest = new MemberStatusChange.Request(
			memberId,
			request.status()
		);

		memberStatusChangeService.changeStatus(serviceRequest);

		return ResponseEntity.noContent().build();
	}

	public record ChangeStatusRequest(
		MemberStatus status
	) {
	}
}
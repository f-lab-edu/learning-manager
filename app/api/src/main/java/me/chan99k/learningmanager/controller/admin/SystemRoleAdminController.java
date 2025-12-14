package me.chan99k.learningmanager.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.admin.GrantSystemRole;
import me.chan99k.learningmanager.admin.RetrieveSystemRole;
import me.chan99k.learningmanager.admin.RevokeSystemRole;
import me.chan99k.learningmanager.controller.admin.requests.GrantRoleRequest;
import me.chan99k.learningmanager.member.SystemRole;

@RestController
@RequestMapping("/api/v1/admin/members")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
public class SystemRoleAdminController {

	private final GrantSystemRole grantSystemRole;
	private final RevokeSystemRole revokeSystemRole;
	private final RetrieveSystemRole retrieveSystemRole;

	public SystemRoleAdminController(GrantSystemRole grantSystemRole, RevokeSystemRole revokeSystemRole,
		RetrieveSystemRole retrieveSystemRole) {
		this.grantSystemRole = grantSystemRole;
		this.revokeSystemRole = revokeSystemRole;
		this.retrieveSystemRole = retrieveSystemRole;
	}

	@PostMapping("/{memberId}/roles")
	public ResponseEntity<Void> grantRole(
		@PathVariable Long memberId,
		@Valid @RequestBody GrantRoleRequest request
	) {
		grantSystemRole.grant(new GrantSystemRole.Request(memberId, request.role()));

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/{memberId}/roles/{role}")
	public ResponseEntity<Void> revokeRole(
		@PathVariable Long memberId,
		@PathVariable SystemRole role
	) {
		revokeSystemRole.revoke(new RevokeSystemRole.Request(memberId, role));

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{memberId}/roles")
	public ResponseEntity<RetrieveSystemRole.Response> getRoles(
		@PathVariable Long memberId) {

		return ResponseEntity.ok(retrieveSystemRole.retrieve(memberId));
	}

}

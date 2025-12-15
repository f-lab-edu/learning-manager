package me.chan99k.learningmanager.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.chan99k.learningmanager.admin.GrantSystemRole;
import me.chan99k.learningmanager.admin.RetrieveSystemRole;
import me.chan99k.learningmanager.admin.RevokeSystemRole;
import me.chan99k.learningmanager.controller.admin.requests.GrantRoleRequest;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.security.CustomUserDetails;

@Tag(name = "System Role Admin", description = "시스템 역할 관리 API (ADMIN, SUPERVISOR 전용)")
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

	@Operation(
		summary = "시스템 역할 부여",
		description = "특정 회원에게 시스템 역할을 부여합니다. SUPERVISOR는 ADMIN 역할을 부여할 수 없습니다."
	)
	// TODO :: API 문서 만든다고 너무 정신 없는데 xml 설정으로 못 빼내는지 확인해보기
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "역할 부여 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 (회원 없음, 권한 에스컬레이션 시도 등)"),
		@ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 또는 SUPERVISOR만 접근 가능)")
	})
	@PostMapping("/{memberId}/roles")
	public ResponseEntity<Void> grantRole(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long memberId,
		@Valid @RequestBody GrantRoleRequest request
	) {
		grantSystemRole.grant(
			new GrantSystemRole.Request(
				memberId,
				request.role(),
				user.getMemberId(),
				request.reason()
			));

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Operation(
		summary = "시스템 역할 회수",
		description = "특정 회원의 시스템 역할을 회수합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "역할 회수 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 (회원 없음, 마지막 ADMIN 회수 시도 등)"),
		@ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 또는 SUPERVISOR만 접근 가능)")
	})
	@DeleteMapping("/{memberId}/roles/{role}")
	public ResponseEntity<Void> revokeRole(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long memberId,
		@PathVariable SystemRole role,
		@RequestParam(required = false) String reason
	) {
		revokeSystemRole.revoke(new RevokeSystemRole.Request(
			memberId,
			role,
			user.getMemberId(),
			reason
		));

		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "시스템 역할 조회",
		description = "특정 회원의 모든 시스템 역할을 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "400", description = "회원을 찾을 수 없음"),
		@ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 또는 SUPERVISOR만 접근 가능)")
	})
	@GetMapping("/{memberId}/roles")
	public ResponseEntity<RetrieveSystemRole.Response> getRoles(
		@Parameter(description = "역할을 조회할 회원 ID", required = true) @PathVariable Long memberId
	) {
		return ResponseEntity.ok(retrieveSystemRole.retrieve(memberId));
	}

}

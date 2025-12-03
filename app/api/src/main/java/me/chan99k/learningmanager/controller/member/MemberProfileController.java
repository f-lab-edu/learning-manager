package me.chan99k.learningmanager.controller.member;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.member.MemberProfileRetrieval;
import me.chan99k.learningmanager.member.MemberProfileUpdate;
import me.chan99k.learningmanager.member.MemberWithdrawal;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/members")
public class MemberProfileController {
	private static final Logger log = LoggerFactory.getLogger(MemberProfileController.class);

	private final MemberProfileUpdate memberProfileUpdate;
	private final MemberProfileRetrieval memberProfileRetrieval;
	private final MemberWithdrawal memberWithdrawal;
	private final AsyncTaskExecutor memberTaskExecutor;

	public MemberProfileController(MemberProfileUpdate memberProfileUpdate,
		MemberProfileRetrieval memberProfileRetrieval, MemberWithdrawal memberWithdrawal,
		AsyncTaskExecutor memberTaskExecutor) {
		this.memberProfileUpdate = memberProfileUpdate;
		this.memberProfileRetrieval = memberProfileRetrieval;
		this.memberWithdrawal = memberWithdrawal;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@PostMapping("/profile")
	public ResponseEntity<MemberProfileUpdate.Response> updateProfile(
		@AuthenticationPrincipal CustomUserDetails user,
		@RequestBody MemberProfileUpdate.Request request
	) {
		MemberProfileUpdate.Response response = memberProfileUpdate.updateProfile(user.getMemberId(), request);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/profile")
	public CompletableFuture<ResponseEntity<MemberProfileRetrieval.Response>> getMemberProfile(
		@AuthenticationPrincipal CustomUserDetails user
	) {
		final Long memberId = user.getMemberId();

		return CompletableFuture.supplyAsync(() -> {
			MemberProfileRetrieval.Response profile = memberProfileRetrieval.getProfile(memberId);
			return ResponseEntity.ok(profile);
		}, memberTaskExecutor);
	}

	@GetMapping("/{nickname}/profile-public")
	public CompletableFuture<ResponseEntity<MemberProfileRetrieval.Response>> getMemberProfileByNickname(
		@PathVariable String nickname
	) {
		return CompletableFuture.supplyAsync(() -> {
			MemberProfileRetrieval.Response publicProfile = memberProfileRetrieval.getPublicProfile(nickname);
			return ResponseEntity.ok(publicProfile);
		}, memberTaskExecutor);
	}

	@DeleteMapping("/withdrawal")
	public ResponseEntity<Void> withdrawal(
		@AuthenticationPrincipal CustomUserDetails user
	) {
		memberWithdrawal.withdrawal(user.getMemberId());
		return ResponseEntity.noContent().build();
	}

}

package me.chan99k.learningmanager.adapter.web.member;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.member.provides.MemberProfileRetrieval;
import me.chan99k.learningmanager.application.member.provides.MemberProfileUpdate;
import me.chan99k.learningmanager.common.exception.UnauthenticatedException;

@RestController
@RequestMapping("/api/v1/members")
public class MemberProfileController {
	private static final Logger log = LoggerFactory.getLogger(MemberProfileController.class);

	private final MemberProfileUpdate memberProfileUpdate;
	private final MemberProfileRetrieval memberProfileRetrieval;
	private final AsyncTaskExecutor memberTaskExecutor;

	public MemberProfileController(MemberProfileUpdate memberProfileUpdate,
		MemberProfileRetrieval memberProfileRetrieval, AsyncTaskExecutor memberTaskExecutor) {
		this.memberProfileUpdate = memberProfileUpdate;
		this.memberProfileRetrieval = memberProfileRetrieval;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@PostMapping("/profile")
	public CompletableFuture<ResponseEntity<MemberProfileUpdate.Response>> updateProfile(
		@RequestBody MemberProfileUpdate.Request request
	) {
		final Long memberId = extractMemberIdFromAuthentication();

		return CompletableFuture.supplyAsync(() -> {
			MemberProfileUpdate.Response response = memberProfileUpdate.updateProfile(memberId, request);
			return ResponseEntity.ok(response);
		}, memberTaskExecutor);
	}

	@GetMapping("/profile")
	public CompletableFuture<ResponseEntity<MemberProfileRetrieval.Response>> getMemberProfile() {
		final Long memberId = extractMemberIdFromAuthentication();

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

	private Long extractMemberIdFromAuthentication() {
		return AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new UnauthenticatedException("[System] 사용자 인증이 필요합니다"));
	}

}

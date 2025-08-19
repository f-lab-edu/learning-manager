package me.chan99k.learningmanager.adapter.web.member;

import java.security.Principal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import me.chan99k.learningmanager.application.member.provides.MemberProfileRetrieval;

@RestController
@RequestMapping("/api/v1/members")
public class MemberProfileController {

	private final MemberProfileRetrieval memberProfileRetrieval;
	private final Executor memberTaskExecutor;

	public MemberProfileController(MemberProfileRetrieval memberProfileRetrieval, Executor memberTaskExecutor) {
		this.memberProfileRetrieval = memberProfileRetrieval;
		this.memberTaskExecutor = memberTaskExecutor;
	}

	@GetMapping("/profile")
	public CompletableFuture<ResponseEntity<MemberProfileRetrieval.Response>> getMemberProfile(
		Principal principal
	) {
		if (principal == null || principal.getName() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "[System] 사용자 인증이 필요합니다");
		}

		final long memberId;

		try {
			memberId = Long.parseLong(principal.getName());
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "[System] 유효하지 않은 사용자 식별자 입니다");
		}

		return CompletableFuture.supplyAsync(() -> {
			MemberProfileRetrieval.Response profile = memberProfileRetrieval.getProfile(memberId);
			return ResponseEntity.ok(profile);
		}, memberTaskExecutor);
	}

	@GetMapping("/{nickname}")
	public CompletableFuture<ResponseEntity<MemberProfileRetrieval.Response>> getMemberProfileByNickname(
		@PathVariable String nickname
	) {
		return CompletableFuture.supplyAsync(() -> {
			MemberProfileRetrieval.Response publicProfile = memberProfileRetrieval.getPublicProfile(nickname);
			return ResponseEntity.ok(publicProfile);
		}, memberTaskExecutor);
	}
}

package me.chan99k.learningmanager.application.session.provides;

import jakarta.validation.constraints.NotNull;
import me.chan99k.learningmanager.domain.session.SessionParticipantRole;

/**
 * [P2] 스터디 세션 참여자 관리
 */
public interface SessionParticipantManagement {

	SessionParticipantResponse addParticipant(Long sessionId, AddParticipantRequest request);

	SessionParticipantResponse removeParticipant(RemoveParticipantRequest request);

	SessionParticipantResponse changeParticipantRole(ChangeParticipantRoleRequest request);

	record AddParticipantRequest(
		@NotNull Long memberId,
		@NotNull SessionParticipantRole role
	) {
	}

	record RemoveParticipantRequest(
		@NotNull Long sessionId,
		@NotNull Long memberId
	) {
	}

	record ChangeParticipantRoleRequest(
		@NotNull Long sessionId,
		@NotNull Long memberId,
		@NotNull SessionParticipantRole newRole
	) {
	}

	record ParticipantInfo(
		Long memberId,
		SessionParticipantRole role
	) {
	}

	record ChangeRoleDto(
		@NotNull SessionParticipantRole newRole
	) {
	}

	record SessionParticipantResponse(
		Long sessionId,
		String title,
		java.util.List<ParticipantInfo> participants
	) {
	}
}

package me.chan99k.learningmanager.session;

import jakarta.validation.constraints.NotNull;

/**
 * [P2] 스터디 세션 참여자 관리
 */
public interface SessionParticipantManagement {

	SessionParticipantResponse addParticipant(Long requestedBy, Long sessionId, AddParticipantRequest request);

	SessionParticipantResponse removeParticipant(Long requestedBy, RemoveParticipantRequest request);

	SessionParticipantResponse changeParticipantRole(Long requestedBy, ChangeParticipantRoleRequest request);

	SessionParticipantResponse leaveSession(Long requestedBy, LeaveSessionRequest request);

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

	record LeaveSessionRequest(
		@NotNull Long sessionId
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

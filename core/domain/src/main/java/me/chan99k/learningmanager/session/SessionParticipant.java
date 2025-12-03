package me.chan99k.learningmanager.session;

import static me.chan99k.learningmanager.session.SessionProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Instant;

import me.chan99k.learningmanager.AbstractEntity;

public class SessionParticipant extends AbstractEntity {

	private Long memberId;

	private Session session;

	private SessionParticipantRole role;

	protected SessionParticipant() {
	}

	public static SessionParticipant reconstitute(
		Long id,
		Long memberId,
		SessionParticipantRole role,
		Instant createdAt,
		Long createdBy,
		Instant lastModifiedAt,
		Long lastModifiedBy,
		Long version
	) {
		SessionParticipant participant = new SessionParticipant();
		participant.setId(id);
		participant.memberId = memberId;
		participant.role = role;
		participant.setCreatedAt(createdAt);
		participant.setCreatedBy(createdBy);
		participant.setLastModifiedAt(lastModifiedAt);
		participant.setLastModifiedBy(lastModifiedBy);
		participant.setVersion(version);
		return participant;
	}

	private SessionParticipant(Long memberId, Session session, SessionParticipantRole role) {
		this.session = session;
		this.memberId = memberId;
		this.role = role;
	}

	public static SessionParticipant of(Long memberId, Session session, SessionParticipantRole role) {
		notNull(session, SESSION_REQUIRED.getMessage());
		notNull(memberId, MEMBER_ID_REQUIRED.getMessage());
		notNull(role, PARTICIPANT_ROLE_REQUIRED.getMessage());

		return new SessionParticipant(memberId, session, role);
	}

	public Long getMemberId() {
		return memberId;
	}

	public SessionParticipantRole getRole() {
		return role;
	}

	public void changeRole(SessionParticipantRole newRole) {
		notNull(newRole, PARTICIPANT_ROLE_REQUIRED.getMessage());
		isTrue(this.role != newRole, SAME_ROLE_PARTICIPANT_ALREADY.getMessage());
		this.role = newRole;
	}
}

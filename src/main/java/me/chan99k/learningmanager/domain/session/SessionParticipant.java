package me.chan99k.learningmanager.domain.session;

import static me.chan99k.learningmanager.domain.session.SessionProblemCode.*;
import static org.springframework.util.Assert.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
public class SessionParticipant extends AbstractEntity {
	@Column(nullable = false)
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id")
	private Session session;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantRole role;

	public SessionParticipant() {
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

package me.chan99k.learningmanager.session.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.chan99k.learningmanager.common.MutableEntity;
import me.chan99k.learningmanager.domain.session.SessionParticipantRole;

@Entity
@Table(name = "session_participant")
public class SessionParticipantEntity extends MutableEntity {

	@Column(nullable = false)
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id")
	private SessionEntity session;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantRole role;

	public SessionParticipantEntity() {
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public SessionEntity getSession() {
		return session;
	}

	public void setSession(SessionEntity session) {
		this.session = session;
	}

	public SessionParticipantRole getRole() {
		return role;
	}

	public void setRole(SessionParticipantRole role) {
		this.role = role;
	}
}

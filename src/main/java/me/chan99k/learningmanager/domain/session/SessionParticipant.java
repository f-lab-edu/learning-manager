package me.chan99k.learningmanager.domain.session;

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
		notNull(session, "[System] Session 은 null 일 수 없습니다.");
		notNull(memberId, "[System] Member ID 는 null 일 수 없습니다.");
		notNull(role, "[System] Role 은 null 일 수 없습니다.");

		return new SessionParticipant(memberId, session, role);
	}

	public Long getMemberId() {
		return memberId;
	}

	public SessionParticipantRole getRole() {
		return role;
	}

	public void changeRole(SessionParticipantRole newRole) {
		notNull(newRole, "[System] 새로운 역할은 null일 수 없습니다.");
		isTrue(this.role != newRole, "[System] 이미 해당 역할을 가지고 있습니다.");
		this.role = newRole;
	}
}

package me.chan99k.learningmanager.domain.session;

import static org.springframework.util.Assert.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionParticipant extends AbstractEntity {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id")
	private Session session;

	@Column(nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantRole role;

	private SessionParticipant(Session session, Long memberId, SessionParticipantRole role) {
		this.session = session;
		this.memberId = memberId;
		this.role = role;
	}

	public static SessionParticipant of(Session session, Long memberId, SessionParticipantRole role) {
		notNull(session, "[System] Session 은 null 일 수 없습니다.");
		notNull(memberId, "[System] Member ID 는 null 일 수 없습니다.");
		notNull(role, "[System] Role 은 null 일 수 없습니다.");

		return new SessionParticipant(session, memberId, role);
	}
}

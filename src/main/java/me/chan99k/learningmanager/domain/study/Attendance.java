package me.chan99k.learningmanager.domain.study;

import static org.springframework.util.Assert.*;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends AbstractEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	private Long memberId;

	private Instant checkInTime;

	private Instant checkOutTime;

	/* 도메인 로직 */

	public static Attendance checkIn(Session session, Long memberId) {
		notNull(session, "[System] 출석을 기록할 세션은 필수입니다.");
		notNull(memberId, "[System] 출석을 기록할 회원은 필수입니다.");

		Attendance attendance = new Attendance();
		attendance.session = session;
		attendance.memberId = memberId;
		attendance.checkInTime = Instant.now(); // 입실 시간은 현재 시간
		attendance.checkOutTime = null; // 퇴실 시간은 아직 없음

		return attendance;
	}

	public void checkOut() {
		state(Objects.isNull(checkOutTime), "[System] 이미 퇴실 처리된 출석 기록입니다.");

		this.checkOutTime = Instant.now();

		// checkOutTime이 checkInTime 보다 늦는지 검증
		isTrue(this.checkOutTime.isAfter(this.checkInTime), "[System] 퇴실 시간은 입실 시간보다 빠를 수 없습니다.");
	}
}
package me.chan99k.learningmanager.domain.attendance;

import static org.springframework.util.Assert.*;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends AbstractEntity {

	private Long sessionId;

	private Long memberId;

	private Instant checkInTime;

	private Instant checkOutTime;

	/* 도메인 로직 */

	public static Attendance checkIn(Long sessionId, Long memberId) {
		notNull(sessionId, "[System] 출석을 기록할 세션은 필수입니다.");
		notNull(memberId, "[System] 출석을 기록할 회원은 필수입니다.");

		Attendance attendance = new Attendance();
		attendance.sessionId = sessionId;
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
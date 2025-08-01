package me.chan99k.learningmanager.domain.attendance;

import static me.chan99k.learningmanager.domain.attendance.AttendanceProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Entity;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity

public class Attendance extends AbstractEntity {

	private Long sessionId;

	private Long memberId;

	private Instant checkInTime;

	private Instant checkOutTime;

	/* 도메인 로직 */

	public static Attendance checkIn(Long sessionId, Long memberId) {
		notNull(sessionId, SESSION_ID_REQUIRED.getMessage());
		notNull(memberId, MEMBER_ID_REQUIRED.getMessage());

		Attendance attendance = new Attendance();
		attendance.sessionId = sessionId;
		attendance.memberId = memberId;
		attendance.checkInTime = Instant.now(); // 입실 시간은 현재 시간
		attendance.checkOutTime = null; // 퇴실 시간은 아직 없음

		return attendance;
	}

	public void checkOut() {
		state(Objects.isNull(checkOutTime), ALREADY_CHECKED_OUT.getMessage());

		this.checkOutTime = Instant.now();

		// checkOutTime이 checkInTime 보다 늦는지 검증
		isTrue(this.checkOutTime.isAfter(this.checkInTime), CHECK_OUT_TIME_BEFORE_CHECK_IN_TIME.getMessage());
	}

	public Long getSessionId() {
		return sessionId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Instant getCheckInTime() {
		return checkInTime;
	}

	public Instant getCheckOutTime() {
		return checkOutTime;
	}
}
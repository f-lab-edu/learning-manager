package me.chan99k.learningmanager.domain.attendance;

import static me.chan99k.learningmanager.domain.attendance.AttendanceProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class Attendance {

	private final Long sessionId;
	private final Long memberId;
	private String id;
	private List<AttendanceEvent> events;

	private AttendanceStatus finalStatus;        // 계산된 최종 상태

	private Attendance(String id, Long sessionId, Long memberId) {
		notNull(sessionId, SESSION_ID_REQUIRED.getMessage());
		notNull(memberId, MEMBER_ID_REQUIRED.getMessage());

		this.id = id;
		this.sessionId = sessionId;
		this.memberId = memberId;
		this.events = new ArrayList<>();
		this.finalStatus = AttendanceStatus.ABSENT;
	}

	public static Attendance create(Long sessionId, Long memberId) {
		return new Attendance(null, sessionId, memberId);
	}

	public static Attendance restore(
		String id, Long sessionId, Long memberId,
		List<AttendanceEvent> events
	) {
		Attendance attendance = new Attendance(id, sessionId, memberId);
		attendance.events = new ArrayList<>(events);
		attendance.recalculateStatus();
		return attendance;
	}

	public void checkIn(Clock clock) {
		validateNotAlreadyCheckedIn();

		AttendanceEvent event = AttendanceEvent.checkIn(clock);
		events.add(event);
		recalculateStatus();
	}

	/* 도메인 로직 */

	public void checkOut(Clock clock) {
		validateAlreadyCheckedIn();

		AttendanceEvent event = AttendanceEvent.checkOut(clock);
		events.add(event);
		recalculateStatus();
	}

	private void recalculateStatus() {
		boolean hasCheckIn = events.stream().anyMatch(event -> event instanceof CheckedIn);

		this.finalStatus = hasCheckIn ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
	}

	/**
	 * 아직 체크인 상태가 아님을 보장하는 메서드
	 */
	private void validateNotAlreadyCheckedIn() {
		boolean isCurrentlyCheckedIn = getCurrentAttendanceState() == AttendanceState.CHECKED_IN;

		if (isCurrentlyCheckedIn) {
			throw new IllegalStateException(ALREADY_CHECKED_IN.getMessage());
		}
	}

	/**
	 * 체크인 상태임을 보장하는 메서드
	 */
	private void validateAlreadyCheckedIn() {
		boolean isCurrentlyCheckedIn = getCurrentAttendanceState() == AttendanceState.CHECKED_IN;

		if (!isCurrentlyCheckedIn) {
			throw new IllegalStateException(NOT_CHECKED_IN.getMessage());
		}

	}

	private AttendanceState getCurrentAttendanceState() {
		if (events.isEmpty()) {
			return AttendanceState.NOT_CHECKED_IN;
		}

		AttendanceEvent lastEvent = events.get(events.size() - 1);
		if (lastEvent instanceof CheckedIn) {
			return AttendanceState.CHECKED_IN;
		} else if (lastEvent instanceof CheckedOut) {
			return AttendanceState.CHECKED_OUT;
		}

		return AttendanceState.NOT_CHECKED_IN;
	}

	public String getId() {
		return id;
	}

	/* 접근 & 수정자 로직 */

	public void setId(String id) {
		if (this.id != null) {
			throw new IllegalStateException(AttendanceProblemCode.CANNOT_REASSIGN_ID.getMessage());
		}
		this.id = id;

	}

	public Long getSessionId() {
		return sessionId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public List<AttendanceEvent> getEvents() {
		return events;
	}

	public AttendanceStatus getFinalStatus() {
		return finalStatus;
	}

	private enum AttendanceState {
		NOT_CHECKED_IN, CHECKED_IN, CHECKED_OUT
	}
}
package me.chan99k.learningmanager.attendance;

import static me.chan99k.learningmanager.attendance.AttendanceProblemCode.*;
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

	public void requestCorrection(
		AttendanceStatus requestedStatus,
		String reason,
		Long requestedBy,
		Clock clock
	) {
		validateNoPendingRequest();  // 이미 대기 중인 요청이 있으면 예외
		validateStatusChange(requestedStatus);  // 같은 상태로 변경 요청 방지

		AttendanceEvent event = AttendanceEvent.correctionRequested(
			clock, this.finalStatus, requestedStatus, reason, requestedBy
		);
		events.add(event);// 출석 상태 요청만 기록하고 finalStatus는 변경하지 않음 (승인 전까지)
	}

	public void approveCorrection(Long approvedBy, Clock clock) {
		CorrectionRequested pendingRequest = getPendingRequest();  // 없으면 예외

		AttendanceEvent event = AttendanceEvent.statusCorrected(
			clock,
			pendingRequest.currentStatus(),
			pendingRequest.requestedStatus(),
			pendingRequest.reason(),
			approvedBy
		);
		events.add(event);
		this.finalStatus = pendingRequest.requestedStatus();  // 상태 변경!
	}

	public void rejectCorrection(String rejectionReason, Long rejectedBy, Clock clock) {
		validateHasPendingRequest();  // 대기 중인 요청이 없으면 예외

		AttendanceEvent event = AttendanceEvent.correctionRejected(
			clock, rejectionReason, rejectedBy
		);
		events.add(event);
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

	/**
	 * events를 역순으로 탐색하여 대기 중인 출석 상태 수정 요청이 있는지 확인
	 */
	private boolean hasPendingRequest() {
		for (int i = events.size() - 1; i >= 0; i--) {
			AttendanceEvent event = events.get(i);

			if (event instanceof CorrectionRequested) {
				return true;  // 요청이 있고, 아직 처리 안됨
			}

			if (event instanceof StatusCorrected || event instanceof CorrectionRejected) {
				return false;  // 이미 처리된 요청
			}
		}

		return false;  // 요청 자체가 없음

	}

	private void validateNoPendingRequest() {
		if (hasPendingRequest()) {
			throw new IllegalStateException(PENDING_REQUEST_EXISTS.getMessage());
		}
	}

	private void validateHasPendingRequest() {
		if (!hasPendingRequest()) {
			throw new IllegalStateException(NO_PENDING_REQUEST.getMessage());
		}
	}

	private CorrectionRequested getPendingRequest() {
		for (int i = events.size() - 1; i >= 0; i--) {
			AttendanceEvent event = events.get(i);

			if (event instanceof CorrectionRequested requested) {
				return requested;
			}

			if (event instanceof StatusCorrected || event instanceof CorrectionRejected) {
				break;  // 이미 처리됨, 더 볼 필요 없음
			}
		}

		throw new IllegalStateException(NO_PENDING_REQUEST.getMessage());
	}

	private void validateStatusChange(AttendanceStatus requestedStatus) {
		if (this.finalStatus == requestedStatus) {
			throw new IllegalStateException(SAME_STATUS_REQUEST.getMessage());
		}
	}

	/* 접근 & 수정자 로직 */

	public String getId() {
		return id;
	}

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

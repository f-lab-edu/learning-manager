package me.chan99k.learningmanager.attendance;

import static me.chan99k.learningmanager.attendance.AttendanceProblemCode.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AttendanceTest {
	private String attendanceId;
	private Long sessionId;
	private Long memberId;

	@BeforeEach
	void setUp() {
		attendanceId = "test-attendace-id";
		sessionId = 1L;
		memberId = 1L;
	}

	@Nested
	@DisplayName("출석(입실/퇴실) 처리 테스트")
	class CheckInAndOut {

		@Test
		@DisplayName("[Success] checkIn 으로 입실 기록을 성공적으로 생성한다.")
		void checkIn_success() {
			Clock clock = Clock.systemUTC();

			Instant beforeCheckIn = clock.instant();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.setId(attendanceId); // 딱 한번 설정 가능
			attendance.checkIn(clock);
			Instant afterCheckIn = clock.instant();

			assertThat(attendance.getEvents()).hasSize(1);
			assertThat(attendance.getEvents().get(0)).isInstanceOf(CheckedIn.class);

			CheckedIn checkInEvent = (CheckedIn)attendance.getEvents().get(0);
			assertThat(checkInEvent.timestamp()).isBetween(beforeCheckIn, afterCheckIn);

			assertThat(attendance.getFinalStatus()).isEqualTo(AttendanceStatus.PRESENT);

		}

		@Test
		@DisplayName("[Success] checkOut 으로 퇴실 시간을 성공적으로 기록한다.")
		void checkOut_success() {
			Clock clock = Clock.systemUTC();

			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);

			// 초기 입실 상태 확인
			assertThat(attendance.getEvents()).hasSize(1);
			assertThat(attendance.getEvents().get(0)).isInstanceOf(CheckedIn.class);

			// 퇴실
			Instant beforeCheckOut = clock.instant();
			attendance.checkOut(clock);
			Instant afterCheckOut = clock.instant();

			// 퇴실 이벤트 확인
			assertThat(attendance.getEvents()).hasSize(2);
			assertThat(attendance.getEvents().get(1)).isInstanceOf(CheckedOut.class);

			CheckedOut checkOutEvent = (CheckedOut)attendance.getEvents().get(1);
			assertThat(checkOutEvent.timestamp()).isBetween(beforeCheckOut, afterCheckOut);

			// 퇴실 시간이 입실 시간보다 늦는지 확인
			CheckedIn checkInEvent = (CheckedIn)attendance.getEvents().get(0);
			assertThat(checkOutEvent.timestamp()).isAfter(checkInEvent.timestamp());

			// 상태는 출석
			assertThat(attendance.getFinalStatus()).isEqualTo(AttendanceStatus.PRESENT);

		}

		@Test
		@DisplayName("[Failure] 이미 퇴실 처리된 기록에 다시 checkOut을 호출하면 예외가 발생한다.")
		void checkOut_fail_if_already_checked_out() {
			Clock clock = Clock.systemUTC();

			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);
			attendance.checkOut(clock); // 첫 번째 퇴실 처리

			// 두 번째 퇴실 시도 시 예외 발생
			assertThatThrownBy(() -> attendance.checkOut(clock))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NOT_CHECKED_IN.getMessage());

		}

		@Test
		@DisplayName("[Failure] 입실하지 않고 바로 checkOut을 호출하면 예외가 발생한다.")
		void checkOut_fail_if_not_checked_in() {
			Clock clock = Clock.systemUTC();

			Attendance attendance = Attendance.create(sessionId, memberId);

			assertThatThrownBy(() -> attendance.checkOut(clock))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NOT_CHECKED_IN.getMessage());
		}

		@Test
		@DisplayName("[Failure] 이미 입실 상태에서 다시 checkIn을 호출하면 예외가 발생한다.")
		void checkIn_fail_if_already_checked_in() {
			Clock clock = Clock.systemUTC();

			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock); // 첫 번째 입실

			// 두 번째 입실 시도 시 예외 발생
			assertThatThrownBy(() -> attendance.checkIn(clock))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(ALREADY_CHECKED_IN.getMessage());
		}

	}

	@Nested
	@DisplayName("Attendance 생성 테스트")
	class Creation {

		@Test
		@DisplayName("[Success] 올바른 sessionId와 memberId로 Attendance를 생성한다.")
		void create_success() {
			Attendance attendance = Attendance.create(sessionId, memberId);

			assertThat(attendance.getSessionId()).isEqualTo(sessionId);
			assertThat(attendance.getMemberId()).isEqualTo(memberId);
			assertThat(attendance.getEvents()).isEmpty();
			assertThat(attendance.getFinalStatus()).isEqualTo(AttendanceStatus.ABSENT);
			assertThat(attendance.getId()).isNull(); // 아직 ID 미할당
		}

		@Test
		@DisplayName("[Failure] sessionId가 null이면 예외가 발생한다.")
		void create_fail_if_sessionId_is_null() {
			assertThatThrownBy(() -> Attendance.create(null, memberId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(SESSION_ID_REQUIRED.getMessage());
		}

		@Test
		@DisplayName("[Failure] memberId가 null이면 예외가 발생한다.")
		void create_fail_if_memberId_is_null() {
			assertThatThrownBy(() -> Attendance.create(sessionId, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(MEMBER_ID_REQUIRED.getMessage());
		}
	}

	@Nested
	@DisplayName("ID 설정 테스트")
	class IdManagement {

		@Test
		@DisplayName("[Success] setId로 ID를 단 한 번 설정할 수 있다.")
		void setId_success() {
			Attendance attendance = Attendance.create(sessionId, memberId);
			String testId = "test-attendance-id";

			attendance.setId(testId);

			assertThat(attendance.getId()).isEqualTo(testId);
		}

		@Test
		@DisplayName("[Failure] 이미 설정된 ID를 다시 설정하면 예외가 발생한다.")
		void setId_fail_if_already_set() {
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.setId("first-id");

			assertThatThrownBy(() -> attendance.setId("second-id"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(AttendanceProblemCode.CANNOT_REASSIGN_ID.getMessage());
		}
	}

	@Nested
	@DisplayName("출석 수정 요청 테스트")
	class CorrectionRequest {

		@Test
		@DisplayName("[Success] 수정 요청을 성공적으로 생성한다")
		void requestCorrection_success() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);
			Long requestedBy = 999L;

			attendance.requestCorrection(AttendanceStatus.LATE, "지각 처리 요청", requestedBy, clock);

			assertThat(attendance.getEvents()).hasSize(2);
			assertThat(attendance.getEvents().get(1)).isInstanceOf(CorrectionRequested.class);

			CorrectionRequested event = (CorrectionRequested)attendance.getEvents().get(1);

			assertThat(event.currentStatus()).isEqualTo(AttendanceStatus.PRESENT);
			assertThat(event.requestedStatus()).isEqualTo(AttendanceStatus.LATE);
			assertThat(event.reason()).isEqualTo("지각 처리 요청");
			assertThat(event.requestedBy()).isEqualTo(requestedBy);
			assertThat(attendance.getFinalStatus()).isEqualTo(AttendanceStatus.PRESENT);
		}

		@Test
		@DisplayName("[Failure] 이미 대기 중인 요청이 있으면 예외가 발생한다")
		void requestCorrection_fail_if_pending_exists() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);
			attendance.requestCorrection(AttendanceStatus.LATE, "첫 번째 요청", 1L, clock);

			assertThatThrownBy(() ->
				attendance.requestCorrection(AttendanceStatus.ABSENT, "두 번째 요청", 2L, clock)
			)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(PENDING_REQUEST_EXISTS.getMessage());
		}

		@Test
		@DisplayName("[Failure] 같은 상태로 변경 요청하면 예외가 발생한다")
		void requestCorrection_fail_if_same_status() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);

			assertThatThrownBy(() ->
				attendance.requestCorrection(AttendanceStatus.PRESENT, "같은 상태", 1L, clock)
			)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(SAME_STATUS_REQUEST.getMessage());
		}
	}

	@Nested
	@DisplayName("출석 수정 승인 테스트")
	class CorrectionApproval {

		@Test
		@DisplayName("[Success] 수정 요청을 승인하면 상태가 변경된다")
		void approveCorrection_success() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);
			attendance.requestCorrection(AttendanceStatus.LATE, "지각 처리", 1L, clock);
			Long approvedBy = 999L;

			attendance.approveCorrection(approvedBy, clock);

			assertThat(attendance.getEvents()).hasSize(3);
			assertThat(attendance.getEvents().get(2)).isInstanceOf(StatusCorrected.class);

			StatusCorrected event = (StatusCorrected)attendance.getEvents().get(2);
			assertThat(event.previousStatus()).isEqualTo(AttendanceStatus.PRESENT);
			assertThat(event.newStatus()).isEqualTo(AttendanceStatus.LATE);
			assertThat(event.correctedBy()).isEqualTo(approvedBy);
			assertThat(attendance.getFinalStatus()).isEqualTo(AttendanceStatus.LATE);
		}

		@Test
		@DisplayName("[Failure] 대기 중인 요청이 없으면 예외가 발생한다")
		void approveCorrection_fail_if_no_pending() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);

			assertThatThrownBy(() -> attendance.approveCorrection(1L, clock))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NO_PENDING_REQUEST.getMessage());
		}
	}

	@Nested
	@DisplayName("출석 수정 거절 테스트")
	class CorrectionRejection {

		@Test
		@DisplayName("[Success] 수정 요청을 거절한다")
		void rejectCorrection_success() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);
			attendance.requestCorrection(AttendanceStatus.LATE, "지각 처리", 1L, clock);
			Long rejectedBy = 999L;

			attendance.rejectCorrection("사유 불충분", rejectedBy, clock);

			assertThat(attendance.getEvents()).hasSize(3);
			assertThat(attendance.getEvents().get(2)).isInstanceOf(CorrectionRejected.class);

			CorrectionRejected event = (CorrectionRejected)attendance.getEvents().get(2);
			assertThat(event.rejectionReason()).isEqualTo("사유 불충분");
			assertThat(event.rejectedBy()).isEqualTo(rejectedBy);
			assertThat(attendance.getFinalStatus()).isEqualTo(AttendanceStatus.PRESENT);
		}

		@Test
		@DisplayName("[Success] 거절 후 새로운 수정 요청이 가능하다")
		void canRequestAfterRejection() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);
			attendance.requestCorrection(AttendanceStatus.LATE, "첫 번째", 1L, clock);
			attendance.rejectCorrection("거절", 2L, clock);

			attendance.requestCorrection(AttendanceStatus.ABSENT, "두 번째", 3L, clock);

			assertThat(attendance.getEvents()).hasSize(4);
			assertThat(attendance.getEvents().get(3)).isInstanceOf(CorrectionRequested.class);
		}

		@Test
		@DisplayName("[Failure] 대기 중인 요청이 없으면 예외가 발생한다")
		void rejectCorrection_fail_if_no_pending() {
			Clock clock = Clock.systemUTC();
			Attendance attendance = Attendance.create(sessionId, memberId);
			attendance.checkIn(clock);

			assertThatThrownBy(() -> attendance.rejectCorrection("사유", 1L, clock))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NO_PENDING_REQUEST.getMessage());
		}
	}

}

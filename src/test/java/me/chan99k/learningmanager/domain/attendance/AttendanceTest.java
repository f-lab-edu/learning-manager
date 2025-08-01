package me.chan99k.learningmanager.domain.attendance;

import static me.chan99k.learningmanager.domain.attendance.AttendanceProblemCode.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AttendanceTest {

	private Long sessionId;
	private Long memberId;

	@BeforeEach
	void setUp() {
		sessionId = 1L;
		memberId = 1L;
	}

	@Nested
	@DisplayName("출석(입실/퇴실) 처리 테스트")
	class CheckInAndOut {

		@Test
		@DisplayName("[Success] checkIn 으로 입실 기록을 성공적으로 생성한다.")
		void checkIn_success() {
			Instant beforeCheckIn = Instant.now();
			Attendance attendance = Attendance.checkIn(sessionId, memberId);
			Instant afterCheckIn = Instant.now();

			assertThat(attendance).isNotNull();
			assertThat(attendance.getSessionId()).isEqualTo(sessionId); // sessionId를 직접 비교
			assertThat(attendance.getMemberId()).isEqualTo(memberId);
			assertThat(attendance.getCheckInTime()).isBetween(beforeCheckIn, afterCheckIn);
			assertThat(attendance.getCheckOutTime()).isNull();
		}

		@Test
		@DisplayName("[Success] checkOut 으로 퇴실 시간을 성공적으로 기록한다.")
		void checkOut_success() {
			Attendance attendance = Attendance.checkIn(sessionId, memberId);
			assertThat(attendance.getCheckOutTime()).isNull();

			// 약간의 시간 간격
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			Instant beforeCheckOut = Instant.now();
			attendance.checkOut();
			Instant afterCheckOut = Instant.now();

			assertThat(attendance.getCheckOutTime()).isNotNull();
			assertThat(attendance.getCheckOutTime()).isBetween(beforeCheckOut, afterCheckOut);
			assertThat(attendance.getCheckOutTime()).isAfter(attendance.getCheckInTime());
		}

		@Test
		@DisplayName("[Failure] 이미 퇴실 처리된 기록에 다시 checkOut을 호출하면 예외가 발생한다.")
		void checkOut_fail_if_already_checked_out() {
			Attendance attendance = Attendance.checkIn(sessionId, memberId);
			attendance.checkOut(); // 첫 번째 퇴실 처리

			assertThat(attendance.getCheckOutTime()).isNotNull();
			assertThatThrownBy(attendance::checkOut)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(ALREADY_CHECKED_OUT.getMessage());
		}
	}
}

package me.chan99k.learningmanager.domain.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AttendanceTest {

    private Session session;
    private Long memberId;

    @BeforeEach
    void setUp() {
        // Session은 다른 테스트에서 검증되었으므로, 여기서는 mock 객체를 사용합니다.
        session = mock(Session.class);
        memberId = 1L;
    }

    @Nested
    @DisplayName("출석(입실/퇴실) 처리 테스트")
    class CheckInAndOut {

        @Test
        @DisplayName("[Success] checkIn으로 입실 기록을 성공적으로 생성한다.")
        void checkIn_success() {
            Instant beforeCheckIn = Instant.now();
            Attendance attendance = Attendance.checkIn(session, memberId);
            Instant afterCheckIn = Instant.now();

            assertThat(attendance).isNotNull();
            assertThat(attendance.getSession()).isEqualTo(session);
            assertThat(attendance.getMemberId()).isEqualTo(memberId);
            assertThat(attendance.getCheckInTime()).isBetween(beforeCheckIn, afterCheckIn);
            assertThat(attendance.getCheckOutTime()).isNull();
        }

        @Test
        @DisplayName("[Success] checkOut으로 퇴실 시간을 성공적으로 기록한다.")
        void checkOut_success() {
            Attendance attendance = Attendance.checkIn(session, memberId);
            assertThat(attendance.getCheckOutTime()).isNull();

            // 퇴실 시간을 기록하기 위해 약간의 시간 간격을 둡니다.
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
            Attendance attendance = Attendance.checkIn(session, memberId);
            attendance.checkOut(); // 첫 번째 퇴실 처리

            assertThatThrownBy(() -> {
                attendance.checkOut(); // 두 번째 퇴실 처리 시도
            }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 퇴실 처리된 출석 기록입니다.");
        }
    }
}

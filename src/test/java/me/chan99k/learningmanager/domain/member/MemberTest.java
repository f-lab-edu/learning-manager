package me.chan99k.learningmanager.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.registerDefault();
    }

    @Test
    @DisplayName("[Failure] 활동 중이 아닌 회원을 이용 정지시키려 하면 예외가 발생한다")
    void ban_fails_if_not_active() {
        member.deactivate(); // 활동 중이 아닌 상태로 만듦

        assertThatThrownBy(member::ban)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("활동 중인 회원만 이용 정지될 수 있습니다.");
    }

    @Test
    @DisplayName("[Failure] 휴면 상태가 아닌 회원을 활성화시키려 하면 예외가 발생한다")
    void activate_fails_if_not_inactive() {
        assertThatThrownBy(member::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("휴면 상태의 회원만 활성화할 수 있습니다.");

        member.ban(); // BANNED 상태로 변경

        assertThatThrownBy(member::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("휴면 상태의 회원만 활성화할 수 있습니다.");
    }

}
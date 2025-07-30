package me.chan99k.learningmanager.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MemberTest {

	private Member member;
	private final NicknameGenerator nicknameGenerator = () -> "user_" + UUID.randomUUID().toString().substring(0, 8);

	@BeforeEach
	void setUp() {
		member = Member.registerDefault(nicknameGenerator);
	}

	@Nested
	@DisplayName("회원 관리 테스트")
	class MemberCreationTEst {
		@Test
		@DisplayName("[Success] 회원 ID 반환에 성공한다.")
		void success_to_return_memberId() {
			ReflectionTestUtils.setField(member, "id", 1L);
			assertThat(member.getId()).isNotNull();
		}

		@Test
		@DisplayName("[Success] 프로필 업데이트에 성공한다.")
		void success_to_update_profile() {
			String updatingUrl = "https://updated.com/profile.jpg";
			String updatingIntro = "안녕하세요, 이것은 수정된 자기소개입니다.";

			member.updateProfile(updatingUrl, updatingIntro);
			assertThat(member.getProfileImageUrl()).isEqualTo(updatingUrl);
			assertThat(member.getSelfIntroduction()).isEqualTo(updatingIntro);
		}
	}

	@Nested
	@DisplayName("회원 닉네임 테스트")
	class NicknameTest {
		@Test
		@DisplayName("[Success] 회원 닉네임 변경에 성공한다")
		void update_member_nickname() {
			assertThat(member.getNickname().value().startsWith("user_")).isTrue();
			assertThat(member.getNickname().value().length()).isEqualTo(13);

			Nickname newNickname = new Nickname("변경하려는 닉네임");

			member.changeNickname(newNickname);

			assertThat(member.getNickname()).isEqualTo(newNickname);
		}

		@Test
		@DisplayName("[Failure] 제약조건에 맞지 않는 닉네임이라면 닉네임 변경에 실패한다.")
		void fail_to_update_nickname() {
			// TODO : 닉네임 제약 조건을 추가하고 테스트 코드 작성 필요
		}
	}

	@Nested
	@DisplayName("회원 상태 테스트")
	class StatusTest {
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

		@Test
		@DisplayName("[Success] 휴면 상태인 회원을 활성화하는데 성공한다")
		void success_to_activate_member() {
			member.deactivate(); // 휴면 상태로 변경
			assertThat(member.getStatus()).isEqualTo(MemberStatus.INACTIVE);

			member.activate();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		}

		@Test
		@DisplayName("[Success] 회원 탈퇴에 성공한다.")
		void success_to_withdraw() {
			member.withdraw();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
		}

		@Test
		@DisplayName("[Success] 회원의 밴을 풀어 주는 것에 성공한다.")
		void success_to_unban_banned_user() {
			member.ban();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.BANNED);
			member.unban();
			assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		}

		@Test
		@DisplayName("[Failure] 이미 휴면 상태인 회원을 다시 휴면시키려 하면 예외가 발생한다.")
		void deactivate_fails_if_already_inactive() {
			member.deactivate(); // 휴면 상태로 변경

			assertThatThrownBy(member::deactivate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("이미 휴면 상태의 회원입니다.");
		}

		@Test
		@DisplayName("[Failure] 이미 탈퇴한 회원을 다시 탈퇴시키려 하면 예외가 발생한다.")
		void withdraw_fails_if_already_withdrawn() {
			member.withdraw(); // 탈퇴 상태로 변경

			assertThatThrownBy(member::withdraw)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("이미 탈퇴한 회원입니다.");
		}

		@Test
		@DisplayName("[Failure] 이용 정지 상태가 아닌 회원의 정지를 해제하려 하면 예외가 발생한다.")
		void unban_fails_if_not_banned() {
			// 기본 상태(ACTIVE)에서 unban 시도
			assertThatThrownBy(member::unban)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("이용 정지 상태의 회원만 해제될 수 있습니다.");
		}
	}

	@Nested
	@DisplayName("회원 권한 테스트")
	class RoleTest {
		@Test
		@DisplayName("[Success] 관리자 승급에 성공한다.")
		void success_to_promote_admin() {
			member.promoteToAdmin();
			assertThat(member.getRole()).isEqualTo(SystemRole.ADMIN);
		}

		@Test
		@DisplayName("[Failure] 이미 관리자인 경우, 관리자 승급에 실패한다.")
		void fail_to_promote_admin() {
			member.promoteToAdmin();
			assertThat(member.getRole()).isEqualTo(SystemRole.ADMIN);
			assertThatThrownBy(() -> member.promoteToAdmin()).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("[Success] 회원등급 강등에 성공한다.")
		void success_to_demote_member() {
			member.promoteToAdmin();
			assertThat(member.getRole()).isEqualTo(SystemRole.ADMIN);

			member.demoteToMember();
			assertThat(member.getRole()).isEqualTo(SystemRole.MEMBER);
		}

		@Test
		@DisplayName("[Failure] 더 강등할 등급이 없는 경우, 회원 등급 강등에 실패한다..")
		void fail_to_demote_member() {
			assertThatThrownBy(() -> member.demoteToMember()).isInstanceOf(IllegalStateException.class);
		}
	}
}
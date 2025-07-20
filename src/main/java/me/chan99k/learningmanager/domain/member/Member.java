package me.chan99k.learningmanager.domain.member;

import static org.springframework.util.Assert.*;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends AbstractEntity {

	private SystemRole role;

	private MemberStatus status;

	@Embedded
	private Nickname nickname;

	private String profileImageUrl;

	private String selfIntroduction;

	/* 도메인 로직 */

	public static Member registerDefault() {
		Member member = new Member();
		member.role = SystemRole.MEMBER;
		member.status = MemberStatus.ACTIVE;
		member.nickname = Nickname.generateWithUUID();

		return member;
	}

	public void changeNickname(Nickname nickname) {
		this.nickname = nickname;
	}

	public void updateProfile(String profileImageUrl, String selfIntroduction) {
		this.profileImageUrl = profileImageUrl;
		this.selfIntroduction = selfIntroduction;
	}

	public void promoteToAdmin() {
		state(this.role == SystemRole.MEMBER, "일반 회원만 관리자로 승격될 수 있습니다.");
		this.role = SystemRole.ADMIN;
	}

	public void demoteToMember() {
		state(this.role == SystemRole.ADMIN, "관리자만 일반 회원으로 강등될 수 있습니다.");
		this.role = SystemRole.MEMBER;
	}

	public void deactivate() {
		state(this.status != MemberStatus.INACTIVE, "이미 휴면 상태의 회원입니다.");
		this.status = MemberStatus.INACTIVE;
	}

	public void activate() {
		state(this.status == MemberStatus.INACTIVE, "휴면 상태의 회원만 활성화할 수 있습니다.");
		this.status = MemberStatus.ACTIVE;
	}

	public void withdraw() {
		state(this.status != MemberStatus.WITHDRAWN, "이미 탈퇴한 회원입니다.");
		this.status = MemberStatus.WITHDRAWN;
	}

	public void ban() {
		state(this.status == MemberStatus.ACTIVE, "활동 중인 회원만 이용 정지될 수 있습니다.");
		this.status = MemberStatus.BANNED;
	}

	public void unban() {
		state(this.status == MemberStatus.BANNED, "이용 정지 상태의 회원만 해제될 수 있습니다.");
		this.status = MemberStatus.ACTIVE;
	}
}

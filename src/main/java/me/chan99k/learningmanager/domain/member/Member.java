package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.springframework.util.Assert.*;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
public class Member extends AbstractEntity {

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "nickname", nullable = false, unique = true, length = 20))
	private Nickname nickname;

	@Enumerated(EnumType.STRING)
	private SystemRole role;

	@Enumerated(EnumType.STRING)
	private MemberStatus status;

	private String profileImageUrl;

	private String selfIntroduction;

	/* 도메인 로직 */
	// TODO :: Account 관련 로직들을 정의하고 Account 에 위임하기
	public static Member registerDefault(NicknameGenerator nicknameGenerator) {
		Member member = new Member();
		member.role = SystemRole.MEMBER;
		member.status = MemberStatus.ACTIVE;
		member.nickname = Nickname.generateNickname(nicknameGenerator);

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
		state(this.role == SystemRole.MEMBER, MEMBER_NOT_GENERAL.getMessage());
		this.role = SystemRole.ADMIN;
	}

	public void demoteToMember() {
		state(this.role == SystemRole.ADMIN, MEMBER_NOT_ADMIN.getMessage());
		this.role = SystemRole.MEMBER;
	}

	public void deactivate() {
		state(this.status != MemberStatus.INACTIVE, MEMBER_ALREADY_INACTIVE.getMessage());
		this.status = MemberStatus.INACTIVE;
	}

	public void activate() {
		state(this.status == MemberStatus.INACTIVE, MEMBER_NOT_INACTIVE.getMessage());
		this.status = MemberStatus.ACTIVE;
	}

	public void withdraw() {
		state(this.status != MemberStatus.WITHDRAWN, MEMBER_ALREADY_WITHDRAWN.getMessage());
		this.status = MemberStatus.WITHDRAWN;
	}

	public void ban() {
		state(this.status == MemberStatus.ACTIVE, MEMBER_NOT_ACTIVE.getMessage());
		this.status = MemberStatus.BANNED;
	}

	public void unban() {
		state(this.status == MemberStatus.BANNED, MEMBER_NOT_BANNED.getMessage());
		this.status = MemberStatus.ACTIVE;
	}

	public Nickname getNickname() {
		return nickname;
	}

	public SystemRole getRole() {
		return role;
	}

	public MemberStatus getStatus() {
		return status;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public String getSelfIntroduction() {
		return selfIntroduction;
	}
}

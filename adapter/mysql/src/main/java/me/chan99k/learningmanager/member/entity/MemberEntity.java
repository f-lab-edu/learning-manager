package me.chan99k.learningmanager.member.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import me.chan99k.learningmanager.common.MutableEntity;
import me.chan99k.learningmanager.domain.member.MemberStatus;
import me.chan99k.learningmanager.domain.member.SystemRole;

@Entity
@Table(name = "member")
public class MemberEntity extends MutableEntity {

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccountEntity> accounts = new ArrayList<>();

	@Column(name = "nickname", nullable = false, unique = true, length = 20)
	private String nickname;

	@Enumerated(EnumType.STRING)
	private SystemRole role;

	@Enumerated(EnumType.STRING)
	private MemberStatus status;

	private String profileImageUrl;

	private String selfIntroduction;

	public MemberEntity() {
	}

	public List<AccountEntity> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<AccountEntity> accounts) {
		this.accounts = accounts;
	}

	public void addAccount(AccountEntity account) {
		accounts.add(account);
		account.setMember(this);
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public SystemRole getRole() {
		return role;
	}

	public void setRole(SystemRole role) {
		this.role = role;
	}

	public MemberStatus getStatus() {
		return status;
	}

	public void setStatus(MemberStatus status) {
		this.status = status;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public String getSelfIntroduction() {
		return selfIntroduction;
	}

	public void setSelfIntroduction(String selfIntroduction) {
		this.selfIntroduction = selfIntroduction;
	}
}

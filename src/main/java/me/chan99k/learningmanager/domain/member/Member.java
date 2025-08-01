package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.springframework.util.Assert.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
public class Member extends AbstractEntity {
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Account> accounts = new ArrayList<>();

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "nickname", nullable = false, unique = true, length = 20))
	private Nickname nickname;

	@Enumerated(EnumType.STRING)
	private SystemRole role;

	@Enumerated(EnumType.STRING)
	private MemberStatus status;

	private String profileImageUrl;

	private String selfIntroduction;

	protected Member() {
	}

	/* 도메인 로직 */
	public static Member registerDefault(NicknameGenerator nicknameGenerator) {
		Member member = new Member();
		member.role = SystemRole.MEMBER;
		member.status = MemberStatus.ACTIVE;
		member.nickname = Nickname.generateNickname(nicknameGenerator);

		return member;
	}

	public void addAccount(String email, String rawPassword, PasswordEncoder encoder) {
		Account account = Account.create(this, email, rawPassword, encoder);
		this.accounts.add(account);
	}

	public void changeAccountPassword(Long accountId, String newRawPassword, PasswordEncoder encoder) {
		Account account = findAccountById(accountId);
		account.changePassword(newRawPassword, encoder);
	}

	public void activateAccount(Long accountId) {
		Account account = findAccountById(accountId);
		account.activate();
	}

	public void deactivateAccount(Long accountId) {
		Account account = findAccountById(accountId);
		account.deactivate();
	}

	Account findAccountById(Long accountId) {
		notNull(accountId, ACCOUNT_ID_REQUIRED.getMessage());
		return accounts.stream()
			.filter(account -> accountId.equals(account.getId()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(CANNOT_FOUND_ACCOUNT.getMessage()));
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

	/* 게터 로직 */

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

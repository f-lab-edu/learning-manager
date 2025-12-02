package me.chan99k.learningmanager.member;

import static me.chan99k.learningmanager.member.MemberProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.chan99k.learningmanager.AbstractEntity;
import me.chan99k.learningmanager.exception.DomainException;

public class Member extends AbstractEntity {

	private List<Account> accounts = new ArrayList<>();

	private Nickname nickname;

	private SystemRole role;

	private MemberStatus status;

	private String profileImageUrl;

	private String selfIntroduction;

	protected Member() {
	}

	public static Member reconstitute(
		Long id,
		Nickname nickname,
		SystemRole role,
		MemberStatus status,
		String profileImageUrl,
		String selfIntroduction,
		List<Account> accounts,
		Instant createdAt,
		Long createdBy,
		Instant lastModifiedAt,
		Long lastModifiedBy,
		Long version
	) {
		Member member = new Member();
		member.setId(id);
		member.nickname = nickname;
		member.role = role;
		member.status = status;
		member.profileImageUrl = profileImageUrl;
		member.selfIntroduction = selfIntroduction;
		member.accounts = new ArrayList<>(accounts);
		member.setCreatedAt(createdAt);
		member.setCreatedBy(createdBy);
		member.setLastModifiedAt(lastModifiedAt);
		member.setLastModifiedBy(lastModifiedBy);
		member.setVersion(version);
		return member;
	}

	/* 도메인 로직 */
	public static Member registerDefault(NicknameGenerator nicknameGenerator) {
		Member member = new Member();
		member.role = SystemRole.MEMBER;
		member.status = MemberStatus.PENDING;
		member.nickname = Nickname.generateNickname(nicknameGenerator);

		return member;
	}

	public void addAccount(String email) {
		Account account = Account.create(this, email);
		this.accounts.add(account);
	}

	public void activateAccount(Long accountId) {
		Account account = findAccountById(accountId);
		account.activate();
	}

	public void deactivateAccount(Long accountId) {
		Account account = findAccountById(accountId);
		account.deactivate();
	}

	public Account findAccountByEmail(Email email) {
		return accounts.stream()
			.filter(account -> account.getEmail().equals(email))
			.findFirst()
			.orElseThrow(() -> new DomainException(ACCOUNT_NOT_FOUND));
	}

	public Account findAccountById(Long accountId) {
		notNull(accountId, ACCOUNT_ID_REQUIRED.getMessage());
		return accounts.stream()
			.filter(account -> accountId.equals(account.getId()))
			.findFirst()
			.orElseThrow(() ->
				new IllegalArgumentException(CANNOT_FOUND_ACCOUNT.getMessage()));
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
		state(this.status == MemberStatus.INACTIVE || this.status == MemberStatus.PENDING,
			MEMBER_NOT_PENDING_OR_INACTIVE.getMessage()
		);
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

	public List<Account> getAccounts() {
		return Collections.unmodifiableList(accounts);
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

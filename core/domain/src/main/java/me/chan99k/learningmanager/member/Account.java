package me.chan99k.learningmanager.member;

import static me.chan99k.learningmanager.member.MemberProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.chan99k.learningmanager.AbstractEntity;
import me.chan99k.learningmanager.exception.DomainException;

public class Account extends AbstractEntity {

	private Member member;

	private AccountStatus status;

	private Email email;

	private List<Credential> credentials = new ArrayList<>();

	/* 도메인 로직 */

	protected Account() {
	}

	public static Account reconstitute(
		Long id,
		AccountStatus status,
		Email email,
		List<Credential> credentials,
		Instant createdAt,
		Long createdBy,
		Instant lastModifiedAt,
		Long lastModifiedBy,
		Long version
	) {
		Account account = new Account();
		account.setId(id);
		account.status = status;
		account.email = email;
		account.credentials = new ArrayList<>(credentials);
		account.setCreatedAt(createdAt);
		account.setCreatedBy(createdBy);
		account.setLastModifiedAt(lastModifiedAt);
		account.setLastModifiedBy(lastModifiedBy);
		account.setVersion(version);
		return account;
	}

	private Account(Member member, String email) {
		notNull(member, ACCOUNT_MEMBER_REQUIRED.getMessage());
		this.member = member;
		this.email = new Email(email);
		this.status = AccountStatus.PENDING;
	}

	protected static Account create(Member member, String email) {
		return new Account(member, email);
	}

	/* 도메인 로직 */

	void activate() {
		state(status == AccountStatus.PENDING || status == AccountStatus.INACTIVE,
			ACCOUNT_NOT_PENDING_OR_INACTIVE.getMessage());
		this.status = AccountStatus.ACTIVE;
	}

	void deactivate() {
		state(status == AccountStatus.ACTIVE, ACCOUNT_NOT_ACTIVE.getMessage());
		this.status = AccountStatus.INACTIVE;
	}

	public void addCredential(Credential credential) {
		if (credentials.contains(credential)) {
			throw new DomainException(CREDENTIAL_ALREADY_EXISTS);
		}
		this.credentials.add(credential);
	}

	public Credential findCredentialByType(CredentialType type) {
		return credentials.stream()
			.filter(c -> c.getType() == type)
			.findFirst()
			.orElseThrow(() -> new DomainException(CREDENTIAL_NOT_FOUND));
	}

	public boolean hasCredentialType(CredentialType type) {
		return credentials.stream()
			.anyMatch(c -> c.getType() == type);
	}

	/* 게터 로직 */

	public AccountStatus getStatus() {
		return status;
	}

	public Email getEmail() {
		return email;
	}

	public List<Credential> getCredentials() {
		return Collections.unmodifiableList(credentials);
	}
}

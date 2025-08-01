package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.springframework.util.Assert.*;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
public class Account extends AbstractEntity {
	@ManyToOne
	private Member member;

	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	@Embedded
	@AttributeOverride(name = "address", column = @Column(name = "email", nullable = false, unique = true))
	private Email email;

	@Embedded
	@AttributeOverride(name = "encoded", column = @Column(name = "password", nullable = false, unique = true))
	private Password password;

	/* 도메인 로직 */

	protected Account() {
	}

	private Account(Member member, String email, String rawPassword, PasswordEncoder encoder) {
		notNull(member, ACCOUNT_MEMBER_REQUIRED.getMessage());

		this.email = new Email(email);
		this.password = Password.generatePassword(rawPassword, encoder);
		this.status = AccountStatus.PENDING;
	}

	protected static Account create(Member member, String email, String rawPassword, PasswordEncoder encoder) {
		return new Account(member, email, rawPassword, encoder);
	}

	void changePassword(String password, PasswordEncoder encoder) {
		this.password = Password.generatePassword(password, encoder);
	}

	void activate() {
		state(status == AccountStatus.PENDING || status == AccountStatus.INACTIVE,
			ACCOUNT_NOT_PENDING_OR_INACTIVE.getMessage());
		this.status = AccountStatus.ACTIVE;
	}

	void deactivate() {
		state(status == AccountStatus.ACTIVE, ACCOUNT_NOT_ACTIVE.getMessage());
		this.status = AccountStatus.INACTIVE;
	}

	/* 게터 로직 */

	AccountStatus getStatus() {
		return status;
	}

	Email getEmail() {
		return email;
	}

	Password getPassword() {
		return password;
	}
}

package me.chan99k.learningmanager.domain.member;

import static org.springframework.util.Assert.*;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends AbstractEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	@Embedded
	private Email email;

	@Embedded
	private Password password;

	/* 도메인 로직 */

	public static Account create(Member member, String emailAddress, String rawPassword, PasswordEncoder encoder) {
		notNull(member, "[System] 계정은 반드시 멤버에 속해야 합니다.");

		Account account = new Account();
		account.member = member;
		account.email = new Email(emailAddress);
		account.password = Password.generatePassword(rawPassword, encoder);
		account.status = AccountStatus.PENDING;

		return account;
	}

	public void changePassword(String password, PasswordEncoder encoder) {
		this.password = Password.generatePassword(password, encoder);
	}

	public void activate() {
		state(status == AccountStatus.PENDING || status == AccountStatus.INACTIVE, "[System] 활성 대기/비활성 상태의 계정이 아닙니다.");
		this.status = AccountStatus.ACTIVE;
	}

	public void deactivate() {
		state(status == AccountStatus.ACTIVE, "[System] 활성 상태의 계정이 아닙니다.");
		this.status = AccountStatus.INACTIVE;
	}

}

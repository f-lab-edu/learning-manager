package me.chan99k.learningmanager.domain.auth;

import static org.springframework.util.Assert.*;

import jakarta.persistence.Entity;
import lombok.Getter;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
public class Account extends AbstractEntity {

	private Long memberId;

	private AccountStatus status;

	private Email email;

	private Password password;

	/* 도메인 로직 */

	public static Account create(CreateAccountRequest request, PasswordEncoder encoder) {
		notNull(request.memberId(), "계정은 반드시 멤버에 속해야 합니다.");

		Account account = new Account();
		account.memberId = request.memberId();
		account.email = new Email(request.email());
		account.password = Password.generatePassword(request.password(), encoder);
		account.status = AccountStatus.PENDING;

		return account;
	}

	public void changePassword(String password, PasswordEncoder encoder) {
		this.password = Password.generatePassword(password, encoder);
	}

	public void activate() {
		state(status == AccountStatus.PENDING || status == AccountStatus.INACTIVE, "활성 대기/비활성 상태의 계정이 아닙니다.");
		this.status = AccountStatus.ACTIVE;
	}

	public void deactivate() {
		state(status == AccountStatus.ACTIVE, "활성 상태의 계정이 아닙니다.");
		this.status = AccountStatus.INACTIVE;
	}

}

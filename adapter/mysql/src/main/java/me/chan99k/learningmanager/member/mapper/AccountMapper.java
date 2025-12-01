package me.chan99k.learningmanager.member.mapper;

import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Password;
import me.chan99k.learningmanager.member.entity.AccountEntity;
import me.chan99k.learningmanager.member.entity.MemberEntity;

public final class AccountMapper {

	private AccountMapper() {
	}

	public static AccountEntity toEntity(Account domain, MemberEntity memberEntity) {
		if (domain == null) {
			return null;
		}

		AccountEntity entity = new AccountEntity();
		entity.setId(domain.getId());
		entity.setMember(memberEntity);
		entity.setStatus(domain.getStatus());
		entity.setEmail(domain.getEmail().address());
		entity.setPassword(domain.getPassword().encoded());
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());

		return entity;
	}

	public static Account toDomain(AccountEntity entity) {
		if (entity == null) {
			return null;
		}

		return Account.reconstitute(
			entity.getId(),
			entity.getStatus(),
			Email.of(entity.getEmail()),
			new Password(entity.getPassword()),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}
}

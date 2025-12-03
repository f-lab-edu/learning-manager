package me.chan99k.learningmanager.member.mapper;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.chan99k.learningmanager.member.Account;
import me.chan99k.learningmanager.member.Credential;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.entity.AccountEntity;
import me.chan99k.learningmanager.member.entity.CredentialEntity;
import me.chan99k.learningmanager.member.entity.MemberEntity;

public final class AccountMapper {

	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

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
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());

		List<CredentialEntity> credentialEntities = domain.getCredentials().stream()
			.map(credential -> toCredentialEntity(credential, entity))
			.collect(Collectors.toList());
		entity.setCredentials(credentialEntities);

		return entity;
	}

	public static Account toDomain(AccountEntity entity) {
		if (entity == null) {
			return null;
		}

		List<Credential> credentials = entity.getCredentials() != null
			? entity.getCredentials().stream()
			.map(AccountMapper::toCredentialDomain)
			.collect(Collectors.toList())
			: Collections.emptyList();

		return Account.reconstitute(
			entity.getId(),
			entity.getStatus(),
			Email.of(entity.getEmail()),
			credentials,
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}

	private static CredentialEntity toCredentialEntity(Credential domain, AccountEntity accountEntity) {
		return CredentialEntity.builder()
			.account(accountEntity)
			.type(domain.getType())
			.secret(domain.getSecret())
			.lastUsedAt(domain.getLastUsedAt())
			.build();
	}

	private static Credential toCredentialDomain(CredentialEntity entity) {
		return Credential.reconstitute(
			entity.getType(),
			entity.getSecret(),
			entity.getLastUsedAt() != null
				? entity.getLastUsedAt().atZone(ZONE_ID).toInstant()
				: null
		);
	}
}

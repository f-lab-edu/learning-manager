package me.chan99k.learningmanager.member.mapper;

import java.time.ZoneId;
import java.util.List;

import me.chan99k.learningmanager.member.Account;
import me.chan99k.learningmanager.member.Credential;
import me.chan99k.learningmanager.member.entity.AccountEntity;
import me.chan99k.learningmanager.member.entity.CredentialEmbeddable;
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

		List<CredentialEmbeddable> credentialList = domain.getCredentials().stream()
			.map(AccountMapper::toCredentialEmbeddable)
			.toList();
		entity.setCredentials(credentialList);

		return entity;
	}

	public static Account toDomain(AccountEntity entity) {
		if (entity == null) {
			return null;
		}

		List<Credential> credentials = entity.getCredentials() != null
			? entity.getCredentials().stream()
			.map(AccountMapper::toCredentialDomain)
			.toList()
			: List.of();

		return Account.reconstitute(
			entity.getId(),
			entity.getStatus(),
			entity.getEmail(),
			credentials,
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}

	private static CredentialEmbeddable toCredentialEmbeddable(Credential domain) {
		return CredentialEmbeddable.builder()
			.type(domain.getType())
			.secret(domain.getSecret())
			.lastUsedAt(domain.getLastUsedAt())
			.build();
	}

	private static Credential toCredentialDomain(CredentialEmbeddable entity) {
		return Credential.reconstitute(
			entity.getType(),
			entity.getSecret(),
			entity.getLastUsedAt() != null
				? entity.getLastUsedAt().atZone(ZONE_ID).toInstant()
				: null
		);
	}
}

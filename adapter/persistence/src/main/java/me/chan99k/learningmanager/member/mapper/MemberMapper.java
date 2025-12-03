package me.chan99k.learningmanager.member.mapper;

import java.util.List;

import me.chan99k.learningmanager.member.Account;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.Nickname;
import me.chan99k.learningmanager.member.entity.AccountEntity;
import me.chan99k.learningmanager.member.entity.MemberEntity;

public final class MemberMapper {

	private MemberMapper() {
	}

	public static MemberEntity toEntity(Member domain) {
		if (domain == null) {
			return null;
		}

		MemberEntity entity = new MemberEntity();
		entity.setId(domain.getId());
		entity.setPrimaryEmail(domain.getPrimaryEmail());
		entity.setNickname(domain.getNickname().value());
		entity.setRole(domain.getRole());
		entity.setStatus(domain.getStatus());
		entity.setProfileImageUrl(domain.getProfileImageUrl());
		entity.setSelfIntroduction(domain.getSelfIntroduction());
		entity.setCreatedAt(domain.getCreatedAt());
		entity.setCreatedBy(domain.getCreatedBy());
		entity.setLastModifiedAt(domain.getLastModifiedAt());
		entity.setLastModifiedBy(domain.getLastModifiedBy());
		entity.setVersion(domain.getVersion());

		List<AccountEntity> accountEntities = domain.getAccounts().stream()
			.map(account -> AccountMapper.toEntity(account, entity))
			.toList();
		entity.setAccounts(new java.util.ArrayList<>(accountEntities));

		return entity;
	}

	public static Member toDomain(MemberEntity entity) {
		if (entity == null) {
			return null;
		}

		List<Account> accounts = entity.getAccounts().stream()
			.map(AccountMapper::toDomain)
			.toList();

		return Member.reconstitute(
			entity.getId(),
			entity.getPrimaryEmail(),
			Nickname.of(entity.getNickname()),
			entity.getRole(),
			entity.getStatus(),
			entity.getProfileImageUrl(),
			entity.getSelfIntroduction(),
			accounts,
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getLastModifiedAt(),
			entity.getLastModifiedBy(),
			entity.getVersion()
		);
	}
}

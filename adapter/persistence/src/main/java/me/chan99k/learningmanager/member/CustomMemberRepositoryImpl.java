package me.chan99k.learningmanager.member;

import static me.chan99k.learningmanager.member.entity.QAccountEntity.*;
import static me.chan99k.learningmanager.member.entity.QMemberEntity.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import me.chan99k.learningmanager.member.entity.MemberEntity;
import me.chan99k.learningmanager.member.mapper.MemberMapper;

@Repository
public class CustomMemberRepositoryImpl implements CustomMemberRepository {
	private final JPAQueryFactory queryFactory;

	public CustomMemberRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public List<MemberEmailPair> findMemberEmailPairs(List<Email> emails, Limit limit) {
		var query = queryFactory
			.select(memberEntity, accountEntity.email)
			.from(memberEntity)
			.join(memberEntity.accounts, accountEntity)
			.where(accountEntity.email.in(emails));

		if (limit.isLimited()) {
			query.limit(limit.max());
		}

		return query.fetch().stream()
			.map(tuple -> new MemberEmailPair(
				MemberMapper.toDomain(Objects.requireNonNull(tuple.get(memberEntity))),
				Objects.requireNonNull(tuple.get(accountEntity.email)).address()))
			.toList();
	}

	@Override
	public Optional<MemberEntity> findByAccountsEmail(String email) {
		MemberEntity foundMember = queryFactory.selectFrom(memberEntity)
			.join(memberEntity.accounts, accountEntity)
			.where(accountEntity.email.eq(Email.of(email)))
			.fetchOne();

		return Optional.ofNullable(foundMember);
	}
}

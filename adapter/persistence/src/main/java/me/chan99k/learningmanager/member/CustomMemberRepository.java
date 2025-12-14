package me.chan99k.learningmanager.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.member.entity.MemberEntity;

public interface CustomMemberRepository {

	List<MemberEmailPair> findMemberEmailPairs(List<Email> emails, Limit limit);

	Optional<MemberEntity> findByAccountsEmail(@Param("email") String email);

}

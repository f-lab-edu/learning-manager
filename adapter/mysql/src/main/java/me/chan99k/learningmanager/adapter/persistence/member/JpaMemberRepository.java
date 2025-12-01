package me.chan99k.learningmanager.adapter.persistence.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.application.member.requires.MemberEmailPair;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.Nickname;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

	@Query("SELECT DISTINCT m FROM Member m JOIN FETCH m.accounts a WHERE a.email = :email")
	Optional<Member> findByAccountsEmail(@Param("email") Email email);

	Optional<Member> findByNickname(Nickname nickname);

	@Query(
		"SELECT new me.chan99k.learningmanager.application.member.requires.MemberEmailPair(m, a.email.address) "
			+ "FROM Member m JOIN m.accounts a "
			+ "WHERE a.email IN :emails"
	)
	List<MemberEmailPair> findMembersByEmails(@Param("emails") List<Email> emails, Limit limit);
}


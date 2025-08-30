package me.chan99k.learningmanager.adapter.persistence.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.Nickname;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, Long> {

	@Query("SELECT DISTINCT m FROM Member m JOIN FETCH m.accounts a WHERE a.email = :email")
	Optional<Member> findByAccountsEmail(@Param("email") Email email);

	Optional<Member> findByNickname(Nickname nickname);
}


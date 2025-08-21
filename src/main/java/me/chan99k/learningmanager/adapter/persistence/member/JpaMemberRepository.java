package me.chan99k.learningmanager.adapter.persistence.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, Long> {
	// TODO :: 아래 내용 찾아 보기
	// Hibernate는 SQL ResultSEt 에서 Member row를 중복해서 가져올 수 있다? + EntityGraph
	// DISTINCT 가 디비 쿼리로 나가지 않도록 하는게 좋지 않나
	@Query("SELECT DISTINCT m FROM Member m JOIN FETCH m.accounts a WHERE a.email = :email")
	@QueryHints({
		@QueryHint(name = "org.hibernate.readOnly", value = "true"),
		@QueryHint(name = "org.hibernate.fetchSize", value = "10")
	})
	Optional<Member> findByAccountsEmail(@Param("email") Email email);
}


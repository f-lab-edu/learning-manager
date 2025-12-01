package me.chan99k.learningmanager.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.member.entity.MemberEntity;

public interface JpaMemberRepository extends JpaRepository<MemberEntity, Long> {

	@Query("SELECT DISTINCT m FROM MemberEntity m JOIN FETCH m.accounts a WHERE a.email = :email")
	Optional<MemberEntity> findByAccountsEmail(@Param("email") String email);

	Optional<MemberEntity> findByNickname(String nickname);

	@Query("SELECT DISTINCT m FROM MemberEntity m JOIN FETCH m.accounts a WHERE a.email IN :emails")
	List<MemberEntity> findByAccountsEmailIn(@Param("emails") List<String> emails, Limit limit);
}


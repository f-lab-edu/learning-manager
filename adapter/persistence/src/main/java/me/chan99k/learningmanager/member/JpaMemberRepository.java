package me.chan99k.learningmanager.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.chan99k.learningmanager.member.entity.MemberEntity;

public interface JpaMemberRepository extends JpaRepository<MemberEntity, Long>, CustomMemberRepository {

	Optional<MemberEntity> findByNickname(String nickname);

}

package me.chan99k.learningmanager.application.member.requires;

import org.springframework.data.jpa.repository.JpaRepository;

import me.chan99k.learningmanager.domain.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}

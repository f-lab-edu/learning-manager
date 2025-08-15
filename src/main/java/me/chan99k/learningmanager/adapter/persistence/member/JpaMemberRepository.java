package me.chan99k.learningmanager.adapter.persistence.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.domain.member.Member;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, Long> {

}


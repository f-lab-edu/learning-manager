package me.chan99k.learningmanager.adapter.persistence.member;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.domain.member.Member;

@Repository
public class MemberCommandAdapter implements MemberCommandRepository {

	private final JpaMemberRepository jpaMemberRepository;

	public MemberCommandAdapter(JpaMemberRepository jpaMemberRepository) {
		this.jpaMemberRepository = jpaMemberRepository;
	}

	@Override
	public Member save(Member member) {
		return jpaMemberRepository.save(member);
	}
}

package me.chan99k.learningmanager.adapter.persistence.member;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;

@Repository
public class MemberQueryAdapter implements MemberQueryRepository {
	private final JpaMemberRepository jpaMemberRepository;

	public MemberQueryAdapter(JpaMemberRepository jpaMemberRepository) {
		this.jpaMemberRepository = jpaMemberRepository;
	}

	@Override
	public Optional<Member> findById(Long memberId) {
		return jpaMemberRepository.findById(memberId);
	}

	@Override
	public Optional<Member> findByEmail(Email email) {
		return jpaMemberRepository.findByAccountsEmail(email);
	}
}

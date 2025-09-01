package me.chan99k.learningmanager.adapter.persistence.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.member.requires.MemberEmailPair;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.Nickname;

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

	@Override // TODO : 비밀번호 재설정을 목적으로 사용하는 경우, 스칼라 프로젝션으로 네트워크 트래픽과 메모리 사용량을 줄일 수 있을 것임
	public Optional<Member> findByEmail(Email email) {
		return jpaMemberRepository.findByAccountsEmail(email);
	}

	@Override
	public Optional<Member> findByNickName(Nickname nickname) {
		return jpaMemberRepository.findByNickname(nickname);
	}

	@Override
	public List<MemberEmailPair> findMembersByEmails(List<Email> emails, int limit) {
		return jpaMemberRepository.findMembersByEmails(emails, Limit.of(limit));
	}
}

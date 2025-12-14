package me.chan99k.learningmanager.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.member.mapper.MemberMapper;

@Repository
public class MemberQueryAdapter implements MemberQueryRepository {

	private final JpaMemberRepository jpaMemberRepository;

	public MemberQueryAdapter(JpaMemberRepository jpaMemberRepository) {
		this.jpaMemberRepository = jpaMemberRepository;
	}

	@Override
	public Optional<Member> findById(Long memberId) {
		return jpaMemberRepository.findById(memberId)
			.map(MemberMapper::toDomain);
	}

	@Override
	public Optional<Member> findByEmail(Email email) {
		return jpaMemberRepository.findByAccountsEmail(email.address())
			.map(MemberMapper::toDomain);
	}

	@Override
	public Optional<Member> findByNickName(Nickname nickname) {
		return jpaMemberRepository.findByNickname(nickname.value())
			.map(MemberMapper::toDomain);
	}

	@Override
	public List<MemberEmailPair> findMembersByEmails(List<Email> emails, int limit) {
		return jpaMemberRepository.findMemberEmailPairs(emails, Limit.of(limit));
	}
}

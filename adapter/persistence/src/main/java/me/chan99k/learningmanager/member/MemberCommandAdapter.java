package me.chan99k.learningmanager.member;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.member.entity.MemberEntity;
import me.chan99k.learningmanager.member.mapper.MemberMapper;

@Repository
public class MemberCommandAdapter implements MemberCommandRepository {

	private final JpaMemberRepository jpaMemberRepository;

	public MemberCommandAdapter(JpaMemberRepository jpaMemberRepository) {
		this.jpaMemberRepository = jpaMemberRepository;
	}

	@Override
	public Member save(Member member) {
		MemberEntity entity = MemberMapper.toEntity(member);
		MemberEntity saved = jpaMemberRepository.save(entity);
		return MemberMapper.toDomain(saved);
	}
}

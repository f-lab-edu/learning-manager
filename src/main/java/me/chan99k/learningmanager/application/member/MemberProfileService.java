package me.chan99k.learningmanager.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.member.provides.MemberProfileRetrieval;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.Nickname;

@Service
@Transactional
public class MemberProfileService implements MemberProfileRetrieval {

	private final MemberQueryRepository memberQueryRepository;

	public MemberProfileService(MemberQueryRepository memberQueryRepository) {
		this.memberQueryRepository = memberQueryRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public MemberProfileRetrieval.Response getProfile(Long memberId) {
		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		// 회원 상태 검증이 필요 하다면 여기에 추가

		return new MemberProfileRetrieval.Response(
			member.getProfileImageUrl(),
			member.getSelfIntroduction()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public MemberProfileRetrieval.Response getPublicProfile(String input) {
		var nickname = Nickname.of(input);

		Member member = memberQueryRepository.findByNickname(nickname)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		return new MemberProfileRetrieval.Response(
			member.getProfileImageUrl(),
			member.getSelfIntroduction()
		);
	}
}

package me.chan99k.learningmanager.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.member.provides.MemberProfileRetrieval;
import me.chan99k.learningmanager.application.member.provides.MemberProfileUpdate;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.Nickname;

@Service
@Transactional
public class MemberProfileService implements MemberProfileRetrieval, MemberProfileUpdate {

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;

	public MemberProfileService(MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
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

		Member member = memberQueryRepository.findByNickName(nickname)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		return new MemberProfileRetrieval.Response(
			member.getProfileImageUrl(),
			member.getSelfIntroduction()
		);
	}

	@Override
	public MemberProfileUpdate.Response updateProfile(Long memberId, MemberProfileUpdate.Request request) {
		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		member.updateProfile(request.profileImageUrl(), request.selfIntroduction());
		memberCommandRepository.save(member);

		return new MemberProfileUpdate.Response(member.getId());
	}
}

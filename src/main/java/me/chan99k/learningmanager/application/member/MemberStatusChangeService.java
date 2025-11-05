package me.chan99k.learningmanager.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.member.provides.MemberStatusChange;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.MemberStatus;
import me.chan99k.learningmanager.domain.member.SystemRole;

@Service
@Transactional
public class MemberStatusChangeService implements MemberStatusChange {

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final UserContext userContext;

	public MemberStatusChangeService(
		MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository,
		UserContext userContext) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.userContext = userContext;
	}

	@Override
	public void changeStatus(Request request) {
		Long currentMemberId = userContext.getCurrentMemberId();

		Member currentMember = memberQueryRepository.findById(currentMemberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		if (currentMember.getRole() != SystemRole.ADMIN) {
			throw new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED);
		}

		Member targetMember = memberQueryRepository.findById(request.memberId())
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		MemberStatus newStatus = request.status();
		switch (newStatus) {
			case ACTIVE -> targetMember.activate();
			case INACTIVE -> targetMember.deactivate();
			case BANNED -> targetMember.ban();
			case WITHDRAWN -> targetMember.withdraw();
			default -> throw new AssertionError("Unreachable code: 지원하지 않는 상태 변경입니다: " + newStatus);
		}

		memberCommandRepository.save(targetMember);
	}
}
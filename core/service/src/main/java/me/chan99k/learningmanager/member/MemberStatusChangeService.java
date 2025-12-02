package me.chan99k.learningmanager.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.exception.DomainException;

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
			throw new DomainException(MemberProblemCode.ADMIN_ONLY_ACTION);
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
package me.chan99k.learningmanager.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class MemberStatusChangeService implements MemberStatusChange {

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;

	public MemberStatusChangeService(
		MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
	}

	@Override
	public void changeStatus(Long requestedBy, Request request) {
		Member currentMember = memberQueryRepository.findById(requestedBy)
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
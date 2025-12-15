package me.chan99k.learningmanager.admin;

import java.time.Clock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;

@Service
@Transactional
public class GrantSystemRoleService implements GrantSystemRole {

	private final MemberQueryRepository memberQueryRepository;
	private final SystemAuthorizationPort systemAuthorizationPort;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;

	public GrantSystemRoleService(
		MemberQueryRepository memberQueryRepository, SystemAuthorizationPort systemAuthorizationPort,
		ApplicationEventPublisher eventPublisher, Clock clock
	) {
		this.memberQueryRepository = memberQueryRepository;
		this.systemAuthorizationPort = systemAuthorizationPort;
		this.eventPublisher = eventPublisher;
		this.clock = clock;
	}

	@Override
	public void grant(Request request) {
		Member member = memberQueryRepository.findById(request.memberId())
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));
		systemAuthorizationPort.grantRole(member.getId(), request.role());

		eventPublisher.publishEvent(new SystemRoleChangeEvent.Granted(
			request.memberId(),
			request.role(),
			request.performedBy(),
			clock.instant(),
			request.reason()
		));
	}
}

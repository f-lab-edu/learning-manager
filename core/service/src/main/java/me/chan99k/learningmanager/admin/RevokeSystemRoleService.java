package me.chan99k.learningmanager.admin;

import java.time.Clock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;

@Service
@Transactional
public class RevokeSystemRoleService implements RevokeSystemRole {
	private final SystemAuthorizationPort authorizationPort;
	private final MemberQueryRepository memberQueryRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;

	public RevokeSystemRoleService(
		SystemAuthorizationPort authorizationPort,
		MemberQueryRepository memberQueryRepository,
		ApplicationEventPublisher eventPublisher,
		Clock clock
	) {
		this.authorizationPort = authorizationPort;
		this.memberQueryRepository = memberQueryRepository;
		this.eventPublisher = eventPublisher;
		this.clock = clock;
	}

	@Override
	public void revoke(Request request) {
		var member = memberQueryRepository.findById(request.memberId())
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		authorizationPort.revokeRole(member.getId(), request.role());

		eventPublisher.publishEvent(new SystemRoleChangeEvent.Revoked(
			request.memberId(),
			request.role(),
			request.performedBy(),
			clock.instant(),
			request.reason()
		));
	}
}

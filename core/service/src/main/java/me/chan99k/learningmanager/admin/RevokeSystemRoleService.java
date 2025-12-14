package me.chan99k.learningmanager.admin;

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

	public RevokeSystemRoleService(SystemAuthorizationPort authorizationPort,
		MemberQueryRepository memberQueryRepository) {
		this.authorizationPort = authorizationPort;
		this.memberQueryRepository = memberQueryRepository;
	}

	@Override
	public void revoke(Long revokedBy, Request request) {
		var member = memberQueryRepository.findById(request.memberId()).orElseThrow(() -> new DomainException(
			MemberProblemCode.MEMBER_NOT_FOUND));

		authorizationPort.revokeRole(member.getId(), request.role());
	}
}

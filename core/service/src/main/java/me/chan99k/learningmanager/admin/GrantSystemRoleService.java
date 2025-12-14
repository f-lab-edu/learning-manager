package me.chan99k.learningmanager.admin;

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

	public GrantSystemRoleService(
		MemberQueryRepository memberQueryRepository, SystemAuthorizationPort systemAuthorizationPort
	) {
		this.memberQueryRepository = memberQueryRepository;
		this.systemAuthorizationPort = systemAuthorizationPort;
	}

	@Override
	public void grant(Long grantedBy, Request request) {
		Member member = memberQueryRepository.findById(request.memberId())
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));
		systemAuthorizationPort.grantRole(member.getId(), request.role());
	}
}

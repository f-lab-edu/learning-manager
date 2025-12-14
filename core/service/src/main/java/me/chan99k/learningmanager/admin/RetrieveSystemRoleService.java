package me.chan99k.learningmanager.admin;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.SystemRole;

@Service
@Transactional(readOnly = true)
public class RetrieveSystemRoleService implements RetrieveSystemRole {
	private final SystemAuthorizationPort authorizationPort;
	private final MemberQueryRepository memberQueryRepository;

	public RetrieveSystemRoleService(SystemAuthorizationPort authorizationPort,
		MemberQueryRepository memberQueryRepository) {
		this.authorizationPort = authorizationPort;
		this.memberQueryRepository = memberQueryRepository;
	}

	@Override
	public Response retrieve(Long memberId) {
		var member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		Set<SystemRole> roles = authorizationPort.getRoles(member.getId());

		return new Response(
			member.getId(),
			roles
		);
	}
}

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
	public void revoke(Request request) {
		// NOTE :: ADMIN 이 스스로의 ADMIN 권한을 회수해버릴 수 있음 -> 막을 것인지 둘 것인지 고민중
		var member = memberQueryRepository.findById(request.memberId()).orElseThrow(() -> new DomainException(
			MemberProblemCode.MEMBER_NOT_FOUND));

		authorizationPort.revokeRole(member.getId(), request.role());
	}
}

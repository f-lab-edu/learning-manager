package me.chan99k.learningmanager.authorization;

import java.util.Set;

import me.chan99k.learningmanager.member.SystemRole;

/**
 * 시스템 레벨 역할의 인가를 위한 포트.
 */
public interface SystemAuthorizationPort {

	boolean hasRole(Long memberId, SystemRole role);

	boolean hasAnyRole(Long memberId, Set<SystemRole> roles);

	Set<SystemRole> getRoles(Long memberId);

	void grantRole(Long memberId, SystemRole role);

	void revokeRole(Long memberId, SystemRole role);

	/**
	 * 회원이 지정된 최소 계층 이상의 역할을 가지고 있는지 확인.
	 * 역할 계층: ADMIN(3) > SUPERVISOR(2) > OPERATOR,REGISTRAR,AUDITOR(1) > MEMBER(0)
	 *
	 * @param memberId    회원 ID
	 * @param minimumRole 최소 요구 역할
	 * @return 해당 계층 이상 역할 보유 시 true
	 */
	boolean hasRoleOrHigher(Long memberId, SystemRole minimumRole);

}

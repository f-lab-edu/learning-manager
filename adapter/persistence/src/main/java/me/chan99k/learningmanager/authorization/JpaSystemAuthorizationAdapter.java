package me.chan99k.learningmanager.authorization;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.member.JpaMemberSystemRoleRepository;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.member.SystemRoleHierarchy;
import me.chan99k.learningmanager.member.entity.MemberSystemRoleEntity;

@Repository
public class JpaSystemAuthorizationAdapter implements SystemAuthorizationPort {

	private final JpaMemberSystemRoleRepository roleRepository;
	private final SystemRoleHierarchy roleHierarchy;

	public JpaSystemAuthorizationAdapter(
		JpaMemberSystemRoleRepository roleRepository,
		SystemRoleHierarchy roleHierarchy
	) {
		this.roleRepository = roleRepository;
		this.roleHierarchy = roleHierarchy;
	}

	@Override
	public boolean hasRole(Long memberId, SystemRole role) {
		return roleRepository.existsByMemberIdAndSystemRole(memberId, role);
	}

	@Override
	public boolean hasAnyRole(Long memberId, Set<SystemRole> roles) {
		return roleRepository.existsByMemberIdAndSystemRoleIn(memberId, roles);
	}

	@Override
	public Set<SystemRole> getRoles(Long memberId) {
		return roleRepository.findByMemberId(memberId).stream()
			.map(MemberSystemRoleEntity::getSystemRole)
			.collect(Collectors.toSet());
	}

	@Override
	@Transactional
	public void grantRole(Long memberId, SystemRole role) {
		if (!hasRole(memberId, role)) {
			roleRepository.save(new MemberSystemRoleEntity(memberId, role));
		}
	}

	@Override
	@Transactional
	public void revokeRole(Long memberId, SystemRole role) {
		roleRepository.deleteByMemberIdAndSystemRole(memberId, role);
	}

	@Override
	public boolean hasRoleOrHigher(Long memberId, SystemRole minimumRole) {
		Set<SystemRole> memberRoles = getRoles(memberId);
		return memberRoles.stream()
			.anyMatch(role -> roleHierarchy.isHigherOrEqual(role, minimumRole));
	}

}
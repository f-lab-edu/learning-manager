package me.chan99k.learningmanager.member;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import me.chan99k.learningmanager.member.entity.MemberSystemRoleEntity;

public interface JpaMemberSystemRoleRepository extends JpaRepository<MemberSystemRoleEntity, Long> {

	List<MemberSystemRoleEntity> findByMemberId(Long memberId);

	boolean existsByMemberIdAndSystemRole(Long memberId, SystemRole systemRole);

	boolean existsByMemberIdAndSystemRoleIn(Long memberId, Set<SystemRole> systemRoles);

	void deleteByMemberIdAndSystemRole(Long memberId, SystemRole systemRole);

	void deleteByMemberId(Long memberId);

}
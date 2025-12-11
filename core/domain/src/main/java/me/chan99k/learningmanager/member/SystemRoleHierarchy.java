package me.chan99k.learningmanager.member;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 시스템 역할의 계층 구조를 정의.
 *
 * <pre>
 * 계층 구조 (높은 레벨 → 낮은 레벨):
 *
 * Level 3: ADMIN (전체 시스템 권한)
 *            |
 * Level 2: SUPERVISOR (감독/모니터링)
 *            |
 * Level 1: OPERATOR | REGISTRAR | AUDITOR (동일 레벨, 다른 책임)
 *            |
 * Level 0: MEMBER (기본 사용자)
 * </pre>
 *
 * <p> 상위 역할은 자기 자신과 더 낮은 레벨의 모든 역할을 암시함.</p>
 */
public class SystemRoleHierarchy {

	public int getLevel(SystemRole role) {
		return role.level;
	}

	public boolean isHigherOrEqual(SystemRole role, SystemRole minimumRole) {
		return role.level >= minimumRole.level;
	}

	public boolean isHigher(SystemRole role, SystemRole otherRole) {
		return role.level > otherRole.level;
	}

	public Set<SystemRole> getImpliedRoles(SystemRole role) {
		return Arrays.stream(SystemRole.values())
			.filter(r -> r.level <= role.level)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(SystemRole.class)));
	}

	public Set<SystemRole> getImpliedRoles(Set<SystemRole> roles) {
		int maxLevel = roles.stream()
			.mapToInt(r -> r.level)
			.max()
			.orElse(0);

		return Arrays.stream(SystemRole.values())
			.filter(r -> r.level <= maxLevel)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(SystemRole.class)));
	}

}

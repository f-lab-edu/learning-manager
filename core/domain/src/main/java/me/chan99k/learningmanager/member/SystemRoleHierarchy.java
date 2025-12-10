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
 * <p>암시 규칙: 상위 역할은 자기 자신과 더 낮은 레벨의 모든 역할을 암시함.</p>
 */
public class SystemRoleHierarchy {

	/**
	 * 역할의 계층 레벨 반환.
	 *
	 * @param role 역할
	 * @return 계층 레벨 (0-3)
	 */
	public int getLevel(SystemRole role) {
		return role.level;
	}

	/**
	 * 역할이 최소 요구 역할 이상인지 확인.
	 *
	 * @param role        확인할 역할
	 * @param minimumRole 최소 요구 역할
	 * @return 계층적으로 동등하거나 상위인 경우 true
	 */
	public boolean isHigherOrEqual(SystemRole role, SystemRole minimumRole) {
		return role.level >= minimumRole.level;
	}

	/**
	 * 역할이 다른 역할보다 상위인지 확인.
	 *
	 * @param role      확인할 역할
	 * @param otherRole 비교 대상 역할
	 * @return 계층적으로 상위인 경우 true
	 */
	public boolean isHigher(SystemRole role, SystemRole otherRole) {
		return role.level > otherRole.level;
	}

	/**
	 * 역할이 암시하는 모든 하위 역할 반환 (자기 자신 포함).
	 * <p>
	 * 규칙: 자기 자신 + 자기보다 레벨이 낮은 모든 역할
	 *
	 * @param role 역할
	 * @return 암시되는 역할 집합
	 */
	public Set<SystemRole> getImpliedRoles(SystemRole role) {
		return Arrays.stream(SystemRole.values())
			.filter(r -> r.level <= role.level)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(SystemRole.class)));
	}

	/**
	 * 여러 역할이 암시하는 모든 하위 역할 반환.
	 *
	 * @param roles 역할 집합
	 * @return 암시되는 역할 집합
	 */
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
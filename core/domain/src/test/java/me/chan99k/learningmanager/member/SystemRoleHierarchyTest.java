package me.chan99k.learningmanager.member;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SystemRoleHierarchyTest {

	private SystemRoleHierarchy hierarchy;

	@BeforeEach
	void setUp() {
		hierarchy = new SystemRoleHierarchy();
	}

	@Nested
	@DisplayName("getLevel")
	class GetLevelTest {

		@Test
		@DisplayName("ADMIN의 레벨은 3이다")
		void admin_level_is_3() {
			assertThat(hierarchy.getLevel(SystemRole.ADMIN)).isEqualTo(3);
		}

		@Test
		@DisplayName("SUPERVISOR의 레벨은 2이다")
		void supervisor_level_is_2() {
			assertThat(hierarchy.getLevel(SystemRole.SUPERVISOR)).isEqualTo(2);
		}

		@Test
		@DisplayName("OPERATOR, REGISTRAR, AUDITOR의 레벨은 1이다")
		void level1_roles() {
			assertThat(hierarchy.getLevel(SystemRole.OPERATOR)).isEqualTo(1);
			assertThat(hierarchy.getLevel(SystemRole.REGISTRAR)).isEqualTo(1);
			assertThat(hierarchy.getLevel(SystemRole.AUDITOR)).isEqualTo(1);
		}

		@Test
		@DisplayName("MEMBER의 레벨은 0이다")
		void member_level_is_0() {
			assertThat(hierarchy.getLevel(SystemRole.MEMBER)).isEqualTo(0);
		}
	}

	@Nested
	@DisplayName("isHigherOrEqual")
	class IsHigherOrEqualTest {

		@Test
		@DisplayName("ADMIN은 모든 역할보다 높거나 같다")
		void admin_is_higher_or_equal_to_all() {
			for (SystemRole role : SystemRole.values()) {
				assertThat(hierarchy.isHigherOrEqual(SystemRole.ADMIN, role))
					.as("ADMIN >= %s", role)
					.isTrue();
			}
		}

		@Test
		@DisplayName("MEMBER는 MEMBER보다만 높거나 같다")
		void member_is_only_higher_or_equal_to_member() {
			assertThat(hierarchy.isHigherOrEqual(SystemRole.MEMBER, SystemRole.MEMBER)).isTrue();
			assertThat(hierarchy.isHigherOrEqual(SystemRole.MEMBER, SystemRole.OPERATOR)).isFalse();
			assertThat(hierarchy.isHigherOrEqual(SystemRole.MEMBER, SystemRole.SUPERVISOR)).isFalse();
			assertThat(hierarchy.isHigherOrEqual(SystemRole.MEMBER, SystemRole.ADMIN)).isFalse();
		}

		@Test
		@DisplayName("동일 레벨의 역할끼리는 서로 높거나 같다")
		void same_level_roles_are_equal() {
			assertThat(hierarchy.isHigherOrEqual(SystemRole.OPERATOR, SystemRole.REGISTRAR)).isTrue();
			assertThat(hierarchy.isHigherOrEqual(SystemRole.REGISTRAR, SystemRole.AUDITOR)).isTrue();
			assertThat(hierarchy.isHigherOrEqual(SystemRole.AUDITOR, SystemRole.OPERATOR)).isTrue();
		}

		@Test
		@DisplayName("SUPERVISOR는 ADMIN보다 낮다")
		void supervisor_is_lower_than_admin() {
			assertThat(hierarchy.isHigherOrEqual(SystemRole.SUPERVISOR, SystemRole.ADMIN)).isFalse();
		}
	}

	@Nested
	@DisplayName("isHigher")
	class IsHigherTest {

		@Test
		@DisplayName("ADMIN은 다른 모든 역할보다 높다")
		void admin_is_higher_than_others() {
			assertThat(hierarchy.isHigher(SystemRole.ADMIN, SystemRole.SUPERVISOR)).isTrue();
			assertThat(hierarchy.isHigher(SystemRole.ADMIN, SystemRole.OPERATOR)).isTrue();
			assertThat(hierarchy.isHigher(SystemRole.ADMIN, SystemRole.MEMBER)).isTrue();
		}

		@Test
		@DisplayName("동일한 역할은 자기 자신보다 높지 않다")
		void same_role_is_not_higher() {
			for (SystemRole role : SystemRole.values()) {
				assertThat(hierarchy.isHigher(role, role))
					.as("%s is not higher than itself", role)
					.isFalse();
			}
		}

		@Test
		@DisplayName("동일 레벨의 역할끼리는 서로 높지 않다")
		void same_level_roles_are_not_higher() {
			assertThat(hierarchy.isHigher(SystemRole.OPERATOR, SystemRole.REGISTRAR)).isFalse();
			assertThat(hierarchy.isHigher(SystemRole.REGISTRAR, SystemRole.AUDITOR)).isFalse();
			assertThat(hierarchy.isHigher(SystemRole.AUDITOR, SystemRole.OPERATOR)).isFalse();
		}

		@Test
		@DisplayName("하위 역할은 상위 역할보다 높지 않다")
		void lower_role_is_not_higher() {
			assertThat(hierarchy.isHigher(SystemRole.MEMBER, SystemRole.ADMIN)).isFalse();
			assertThat(hierarchy.isHigher(SystemRole.OPERATOR, SystemRole.SUPERVISOR)).isFalse();
		}
	}

	@Nested
	@DisplayName("getIncludedRoles (단일 역할)")
	class GetIncludedRolesSingleTest {

		@Test
		@DisplayName("ADMIN은 모든 역할의 권한을 포함한다")
		void admin_includes_all_roles() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(SystemRole.ADMIN);

			assertThat(included).containsExactlyInAnyOrder(
				SystemRole.ADMIN,
				SystemRole.SUPERVISOR,
				SystemRole.OPERATOR,
				SystemRole.REGISTRAR,
				SystemRole.AUDITOR,
				SystemRole.MEMBER
			);
		}

		@Test
		@DisplayName("SUPERVISOR는 레벨 2 이하의 역할을 포함한다")
		void supervisor_includes_level2_and_below() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(SystemRole.SUPERVISOR);

			assertThat(included).containsExactlyInAnyOrder(
				SystemRole.SUPERVISOR,
				SystemRole.OPERATOR,
				SystemRole.REGISTRAR,
				SystemRole.AUDITOR,
				SystemRole.MEMBER
			);
			assertThat(included).doesNotContain(SystemRole.ADMIN);
		}

		@Test
		@DisplayName("OPERATOR는 레벨 1 이하의 역할을 포함한다")
		void operator_includes_level1_and_below() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(SystemRole.OPERATOR);

			assertThat(included).containsExactlyInAnyOrder(
				SystemRole.OPERATOR,
				SystemRole.REGISTRAR,
				SystemRole.AUDITOR,
				SystemRole.MEMBER
			);
			assertThat(included).doesNotContain(SystemRole.ADMIN, SystemRole.SUPERVISOR);
		}

		@Test
		@DisplayName("MEMBER는 자기 자신만 포함한다")
		void member_includes_only_itself() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(SystemRole.MEMBER);

			assertThat(included).containsExactly(SystemRole.MEMBER);
		}
	}

	@Nested
	@DisplayName("getIncludedRoles (역할 집합)")
	class GetIncludedRolesSetTest {

		@Test
		@DisplayName("빈 집합은 MEMBER 권한만 포함한다")
		void empty_set_includes_member() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(Set.of());

			assertThat(included).containsExactly(SystemRole.MEMBER);
		}

		@Test
		@DisplayName("여러 역할 중 최고 레벨 기준으로 포함 역할이 결정된다")
		void multiple_roles_use_max_level() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(
				Set.of(SystemRole.OPERATOR, SystemRole.MEMBER)
			);

			assertThat(included).containsExactlyInAnyOrder(
				SystemRole.OPERATOR,
				SystemRole.REGISTRAR,
				SystemRole.AUDITOR,
				SystemRole.MEMBER
			);
		}

		@Test
		@DisplayName("SUPERVISOR와 OPERATOR가 있으면 SUPERVISOR 기준으로 역할을 포함한다")
		void supervisor_and_operator_uses_supervisor_level() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(
				Set.of(SystemRole.SUPERVISOR, SystemRole.OPERATOR)
			);

			assertThat(included).containsExactlyInAnyOrder(
				SystemRole.SUPERVISOR,
				SystemRole.OPERATOR,
				SystemRole.REGISTRAR,
				SystemRole.AUDITOR,
				SystemRole.MEMBER
			);
			assertThat(included).doesNotContain(SystemRole.ADMIN);
		}

		@Test
		@DisplayName("ADMIN이 포함되면 모든 역할의 권한을 갖는다")
		void admin_in_set_includes_all() {
			Set<SystemRole> included = hierarchy.getIncludedRoles(
				Set.of(SystemRole.ADMIN, SystemRole.MEMBER)
			);

			assertThat(included).containsExactlyInAnyOrder(SystemRole.values());
		}
	}
}

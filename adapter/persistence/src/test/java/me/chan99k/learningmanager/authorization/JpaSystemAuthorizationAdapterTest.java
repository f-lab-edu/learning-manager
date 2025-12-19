package me.chan99k.learningmanager.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.member.JpaMemberSystemRoleRepository;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.member.SystemRoleHierarchy;
import me.chan99k.learningmanager.member.entity.MemberSystemRoleEntity;

@DisplayName("JpaSystemAuthorizationAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class JpaSystemAuthorizationAdapterTest {

	private static final Long MEMBER_ID = 1L;
	@Mock
	private JpaMemberSystemRoleRepository roleRepository;
	@Mock
	private SystemRoleHierarchy roleHierarchy;
	private JpaSystemAuthorizationAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new JpaSystemAuthorizationAdapter(roleRepository, roleHierarchy);
	}

	@Nested
	@DisplayName("역할 확인 메서드")
	class RoleCheckTests {

		@Test
		@DisplayName("[Success] hasRole로 특정 역할 보유 여부를 확인한다")
		void test01() {
			when(roleRepository.existsByMemberIdAndSystemRole(MEMBER_ID, SystemRole.ADMIN))
				.thenReturn(true);

			boolean result = adapter.hasRole(MEMBER_ID, SystemRole.ADMIN);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] hasRole로 역할이 없는 경우 false 반환")
		void test02() {
			when(roleRepository.existsByMemberIdAndSystemRole(MEMBER_ID, SystemRole.ADMIN))
				.thenReturn(false);

			boolean result = adapter.hasRole(MEMBER_ID, SystemRole.ADMIN);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Success] hasAnyRole로 여러 역할 중 하나를 보유하는지 확인한다")
		void test03() {
			Set<SystemRole> roles = Set.of(SystemRole.ADMIN, SystemRole.REGISTRAR);
			when(roleRepository.existsByMemberIdAndSystemRoleIn(MEMBER_ID, roles))
				.thenReturn(true);

			boolean result = adapter.hasAnyRole(MEMBER_ID, roles);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Success] getRoles로 회원의 모든 역할을 조회한다")
		void test04() {
			MemberSystemRoleEntity adminRole = new MemberSystemRoleEntity(MEMBER_ID, SystemRole.ADMIN);
			MemberSystemRoleEntity registrarRole = new MemberSystemRoleEntity(MEMBER_ID, SystemRole.REGISTRAR);
			when(roleRepository.findByMemberId(MEMBER_ID))
				.thenReturn(List.of(adminRole, registrarRole));

			Set<SystemRole> result = adapter.getRoles(MEMBER_ID);

			assertThat(result).containsExactlyInAnyOrder(SystemRole.ADMIN, SystemRole.REGISTRAR);
		}
	}

	@Nested
	@DisplayName("역할 부여/철회 메서드")
	class RoleGrantRevokeTests {

		@Test
		@DisplayName("[Success] grantRole로 역할을 부여한다")
		void test01() {
			when(roleRepository.existsByMemberIdAndSystemRole(MEMBER_ID, SystemRole.ADMIN))
				.thenReturn(false);

			adapter.grantRole(MEMBER_ID, SystemRole.ADMIN);

			verify(roleRepository).save(any(MemberSystemRoleEntity.class));
		}

		@Test
		@DisplayName("[Success] grantRole로 이미 역할이 있으면 저장하지 않는다")
		void test02() {
			when(roleRepository.existsByMemberIdAndSystemRole(MEMBER_ID, SystemRole.ADMIN))
				.thenReturn(true);

			adapter.grantRole(MEMBER_ID, SystemRole.ADMIN);

			verify(roleRepository, never()).save(any(MemberSystemRoleEntity.class));
		}

		@Test
		@DisplayName("[Success] revokeRole로 역할을 철회한다")
		void test03() {
			adapter.revokeRole(MEMBER_ID, SystemRole.ADMIN);

			verify(roleRepository).deleteByMemberIdAndSystemRole(MEMBER_ID, SystemRole.ADMIN);
		}
	}

	@Nested
	@DisplayName("역할 계층 확인 메서드")
	class RoleHierarchyTests {

		@Test
		@DisplayName("[Success] hasRoleOrHigher로 최소 역할 이상을 보유하는지 확인한다")
		void test01() {
			MemberSystemRoleEntity adminRole = new MemberSystemRoleEntity(MEMBER_ID, SystemRole.ADMIN);
			when(roleRepository.findByMemberId(MEMBER_ID)).thenReturn(List.of(adminRole));
			when(roleHierarchy.isHigherOrEqual(SystemRole.ADMIN, SystemRole.REGISTRAR)).thenReturn(true);

			boolean result = adapter.hasRoleOrHigher(MEMBER_ID, SystemRole.REGISTRAR);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] hasRoleOrHigher로 최소 역할 이상을 보유하지 않으면 false 반환")
		void test02() {
			MemberSystemRoleEntity registrarRole = new MemberSystemRoleEntity(MEMBER_ID, SystemRole.REGISTRAR);
			when(roleRepository.findByMemberId(MEMBER_ID)).thenReturn(List.of(registrarRole));
			when(roleHierarchy.isHigherOrEqual(SystemRole.REGISTRAR, SystemRole.ADMIN)).thenReturn(false);

			boolean result = adapter.hasRoleOrHigher(MEMBER_ID, SystemRole.ADMIN);

			assertThat(result).isFalse();
		}
	}
}

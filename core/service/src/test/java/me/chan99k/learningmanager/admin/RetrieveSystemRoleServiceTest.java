package me.chan99k.learningmanager.admin;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.SystemRole;

@ExtendWith(MockitoExtension.class)
class RetrieveSystemRoleServiceTest {

	@InjectMocks
	private RetrieveSystemRoleService retrieveSystemRoleService;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private SystemAuthorizationPort authorizationPort;

	@Mock
	private Member member;

	@Test
	@DisplayName("[Success] 회원의 역할 조회에 성공한다")
	void retrieve_Success() {
		Long memberId = 1L;
		Set<SystemRole> expectedRoles = Set.of(SystemRole.OPERATOR, SystemRole.AUDITOR);

		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getId()).thenReturn(memberId);
		when(authorizationPort.getRoles(memberId)).thenReturn(expectedRoles);

		RetrieveSystemRole.Response response = retrieveSystemRoleService.retrieve(memberId);

		assertThat(response.memberId()).isEqualTo(memberId);
		assertThat(response.roles()).containsExactlyInAnyOrderElementsOf(expectedRoles);
	}

	@Test
	@DisplayName("[Success] 역할이 없는 회원 조회 시 빈 집합을 반환한다")
	void retrieve_Success_EmptyRoles() {
		Long memberId = 1L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getId()).thenReturn(memberId);
		when(authorizationPort.getRoles(memberId)).thenReturn(Set.of());

		RetrieveSystemRole.Response response = retrieveSystemRoleService.retrieve(memberId);

		assertThat(response.memberId()).isEqualTo(memberId);
		assertThat(response.roles()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 회원의 역할을 조회 시 예외가 발생한다")
	void retrieve_Fail_MemberNotFound() {
		Long memberId = 999L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> retrieveSystemRoleService.retrieve(memberId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(authorizationPort, never()).getRoles(anyLong());
	}
}

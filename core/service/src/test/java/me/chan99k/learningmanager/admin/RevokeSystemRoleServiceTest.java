package me.chan99k.learningmanager.admin;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

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
class RevokeSystemRoleServiceTest {

	@Mock
	SystemAuthorizationPort authorizationPort;
	@Mock
	MemberQueryRepository memberQueryRepository;
	@InjectMocks
	private RevokeSystemRoleService revokeSystemRoleService;
	@Mock
	private Member member;

	@Test
	@DisplayName("[Success] 관리자가 SystemRole 회수에 성공한다")
	void test01() {
		Long memberId = 1L;
		SystemRole role = SystemRole.OPERATOR;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getId()).thenReturn(memberId);

		RevokeSystemRole.Request request = new RevokeSystemRole.Request(memberId, role);

		revokeSystemRoleService.revoke(request);

		verify(authorizationPort).revokeRole(memberId, role);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 회원의 역할을 회수하려 시도 시 예외가 발생한다")
	void revoke_Fail_MemberNotFound() {
		Long memberId = 999L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.empty());

		RevokeSystemRole.Request request = new RevokeSystemRole.Request(memberId, SystemRole.OPERATOR);

		assertThatThrownBy(() -> revokeSystemRoleService.revoke(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(authorizationPort, never()).revokeRole(anyLong(), any());
	}

}

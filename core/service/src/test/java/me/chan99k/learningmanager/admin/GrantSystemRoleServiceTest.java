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
class GrantSystemRoleServiceTest {

	@InjectMocks
	private GrantSystemRoleService grantSystemRoleService;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private SystemAuthorizationPort systemAuthorizationPort;

	@Mock
	private Member member;

	@Test
	@DisplayName("[Success] 회원에게 역할 부여에 성공한다")
	void grant_Success() {
		Long memberId = 1L;
		SystemRole role = SystemRole.OPERATOR;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getId()).thenReturn(memberId);

		GrantSystemRole.Request request = new GrantSystemRole.Request(memberId, role);

		grantSystemRoleService.grant(request);

		verify(systemAuthorizationPort).grantRole(memberId, role);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 회원에게 역할 부여 시 예외가 발생한다")
	void grant_Fail_MemberNotFound() {
		Long memberId = 999L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.empty());

		GrantSystemRole.Request request = new GrantSystemRole.Request(memberId, SystemRole.OPERATOR);

		assertThatThrownBy(() -> grantSystemRoleService.grant(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(systemAuthorizationPort, never()).grantRole(anyLong(), any());
	}
}

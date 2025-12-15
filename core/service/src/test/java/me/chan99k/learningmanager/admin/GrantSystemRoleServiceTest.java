package me.chan99k.learningmanager.admin;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.SystemRole;

@ExtendWith(MockitoExtension.class)
class GrantSystemRoleServiceTest {

	public static final String REASON = "TEST_GRANT";
	private static final Long SYSTEM_ACCOUNT_ID = 1L;

	@InjectMocks
	private GrantSystemRoleService grantSystemRoleService;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private SystemAuthorizationPort systemAuthorizationPort;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private Member member;

	@Mock
	private Clock clock;

	@Test
	@DisplayName("[Success] 회원에게 역할 부여에 성공한다")
	void grant_Success() {
		Long memberId = 100L;
		SystemRole role = SystemRole.OPERATOR;
		Instant now = Instant.parse("2025-12-16T00:00:00Z");

		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getId()).thenReturn(memberId);
		when(clock.instant()).thenReturn(now);

		GrantSystemRole.Request request = new GrantSystemRole.Request(memberId, role, SYSTEM_ACCOUNT_ID, REASON);

		grantSystemRoleService.grant(request);

		verify(systemAuthorizationPort).grantRole(memberId, role);

		// ArgumentCaptor로 이벤트 캡처
		ArgumentCaptor<SystemRoleChangeEvent.Granted> eventCaptor =
			ArgumentCaptor.forClass(SystemRoleChangeEvent.Granted.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());

		SystemRoleChangeEvent.Granted capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent.memberId()).isEqualTo(memberId);
		assertThat(capturedEvent.role()).isEqualTo(role);
		assertThat(capturedEvent.performedBy()).isEqualTo(SYSTEM_ACCOUNT_ID);
		assertThat(capturedEvent.reason()).isEqualTo(REASON);
		assertThat(capturedEvent.performedAt()).isEqualTo(now);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 회원에게 역할 부여 시 예외가 발생한다")
	void grant_Fail_MemberNotFound() {
		Long memberId = 999L;

		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.empty());

		GrantSystemRole.Request request = new GrantSystemRole.Request(
			memberId, SystemRole.OPERATOR,
			SYSTEM_ACCOUNT_ID,
			REASON
		);

		assertThatThrownBy(() -> grantSystemRoleService.grant(request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(systemAuthorizationPort, never()).grantRole(anyLong(), any());
		verify(eventPublisher, never()).publishEvent(any());
	}
}

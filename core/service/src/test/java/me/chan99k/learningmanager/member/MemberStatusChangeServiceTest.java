package me.chan99k.learningmanager.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class MemberStatusChangeServiceTest {

	@InjectMocks
	private MemberStatusChangeService memberStatusChangeService;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private MemberCommandRepository memberCommandRepository;

	@Mock
	private SystemAuthorizationPort systemAuthorizationPort;

	@Mock
	private Member targetMember;

	static Stream<Arguments> statusChangeProvider() {
		return Stream.of(
			Arguments.of(MemberStatus.ACTIVE, "activate", (Consumer<Member>)Member::activate),
			Arguments.of(MemberStatus.INACTIVE, "deactivate", (Consumer<Member>)Member::deactivate),
			Arguments.of(MemberStatus.BANNED, "ban", (Consumer<Member>)Member::ban),
			Arguments.of(MemberStatus.WITHDRAWN, "withdraw", (Consumer<Member>)Member::withdraw)
		);
	}

	@ParameterizedTest(name = "[Success] 관리자가 회원 상태를 {0}로 변경에 성공한다")
	@MethodSource("statusChangeProvider")
	@DisplayName("[Success] 관리자가 회원 상태 변경에 성공한다")
	void changeStatus_Success(MemberStatus status, String methodName, Consumer<Member> expectedMethodCall) {
		Long adminId = 1L;
		Long targetMemberId = 2L;
		when(systemAuthorizationPort.hasRole(adminId, SystemRole.ADMIN)).thenReturn(true);
		when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.of(targetMember));

		MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, status);

		memberStatusChangeService.changeStatus(adminId, request);

		expectedMethodCall.accept(verify(targetMember));
		verify(memberCommandRepository).save(targetMember);
	}

	@Test
	@DisplayName("[Failure] 관리자가 아닌 사용자는 DomainException이 발생한다")
	void changeStatus_Fail_NotAdmin() {
		Long memberId = 1L;
		when(systemAuthorizationPort.hasRole(memberId, SystemRole.ADMIN)).thenReturn(false);

		MemberStatusChange.Request request = new MemberStatusChange.Request(2L, MemberStatus.BANNED);

		assertThatThrownBy(() -> memberStatusChangeService.changeStatus(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.ADMIN_ONLY_ACTION);

		verify(memberCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 대상 회원에 대해 DomainException이 발생한다")
	void changeStatus_Fail_TargetMemberNotFound() {
		Long adminId = 1L;
		Long targetMemberId = 999L;
		when(systemAuthorizationPort.hasRole(adminId, SystemRole.ADMIN)).thenReturn(true);
		when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.empty());

		MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, MemberStatus.BANNED);

		assertThatThrownBy(() -> memberStatusChangeService.changeStatus(adminId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(memberCommandRepository, never()).save(any());
	}
}

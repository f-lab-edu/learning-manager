package me.chan99k.learningmanager.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.member.provides.MemberStatusChange;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.MemberStatus;
import me.chan99k.learningmanager.domain.member.SystemRole;

@ExtendWith(MockitoExtension.class)
class MemberStatusChangeServiceTest {

	@InjectMocks
	private MemberStatusChangeService memberStatusChangeService;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private MemberCommandRepository memberCommandRepository;

	@Mock
	private Member adminMember;

	@Mock
	private Member targetMember;

	@Test
	@DisplayName("[Success] 관리자가 회원 상태를 BANNED로 변경에 성공한다")
	void changeStatus_Success_BanMember() {
		Long adminId = 1L;
		Long targetMemberId = 2L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(adminId));
			when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(adminMember));
			when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.of(targetMember));
			when(adminMember.getRole()).thenReturn(SystemRole.ADMIN);

			MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, MemberStatus.BANNED);

			memberStatusChangeService.changeStatus(request);

			verify(targetMember).ban();
			verify(memberCommandRepository).save(targetMember);
		}
	}

	@Test
	@DisplayName("[Success] 관리자가 회원 상태를 ACTIVE로 변경에 성공한다")
	void changeStatus_Success_ActivateMember() {
		Long adminId = 1L;
		Long targetMemberId = 2L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(adminId));
			when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(adminMember));
			when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.of(targetMember));
			when(adminMember.getRole()).thenReturn(SystemRole.ADMIN);

			MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, MemberStatus.ACTIVE);

			memberStatusChangeService.changeStatus(request);

			verify(targetMember).activate();
			verify(memberCommandRepository).save(targetMember);
		}
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 AuthenticationException이 발생한다")
	void changeStatus_Fail_Unauthenticated() {
		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			MemberStatusChange.Request request = new MemberStatusChange.Request(2L, MemberStatus.BANNED);

			assertThatThrownBy(() -> memberStatusChangeService.changeStatus(request))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);

			verify(memberQueryRepository, never()).findById(anyLong());
			verify(memberCommandRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("[Failure] 관리자가 아닌 사용자는 AuthorizationException이 발생한다")
	void changeStatus_Fail_NotAdmin() {
		Long memberId = 1L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(memberId));
			when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(adminMember));
			when(adminMember.getRole()).thenReturn(SystemRole.MEMBER);

			MemberStatusChange.Request request = new MemberStatusChange.Request(2L, MemberStatus.BANNED);

			assertThatThrownBy(() -> memberStatusChangeService.changeStatus(request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);

			verify(memberCommandRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 대상 회원에 대해 DomainException이 발생한다")
	void changeStatus_Fail_TargetMemberNotFound() {
		Long adminId = 1L;
		Long targetMemberId = 999L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(adminId));
			when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(adminMember));
			when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.empty());
			when(adminMember.getRole()).thenReturn(SystemRole.ADMIN);

			MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, MemberStatus.BANNED);

			assertThatThrownBy(() -> memberStatusChangeService.changeStatus(request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

			verify(memberCommandRepository, never()).save(any());
		}
	}
}
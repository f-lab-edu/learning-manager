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
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberCommandRepository;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.MemberStatus;
import me.chan99k.learningmanager.member.MemberStatusChange;
import me.chan99k.learningmanager.member.MemberStatusChangeService;
import me.chan99k.learningmanager.member.SystemRole;

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
		// given
		Long adminId = 1L;
		Long targetMemberId = 2L;
		when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(adminMember));
		when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.of(targetMember));
		when(adminMember.getRole()).thenReturn(SystemRole.ADMIN);

		MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, MemberStatus.BANNED);

		// when
		memberStatusChangeService.changeStatus(adminId, request);

		// then
		verify(targetMember).ban();
		verify(memberCommandRepository).save(targetMember);
	}

	@Test
	@DisplayName("[Success] 관리자가 회원 상태를 ACTIVE로 변경에 성공한다")
	void changeStatus_Success_ActivateMember() {
		// given
		Long adminId = 1L;
		Long targetMemberId = 2L;
		when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(adminMember));
		when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.of(targetMember));
		when(adminMember.getRole()).thenReturn(SystemRole.ADMIN);

		MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, MemberStatus.ACTIVE);

		// when
		memberStatusChangeService.changeStatus(adminId, request);

		// then
		verify(targetMember).activate();
		verify(memberCommandRepository).save(targetMember);
	}

	@Test
	@DisplayName("[Failure] 관리자가 아닌 사용자는 DomainException이 발생한다")
	void changeStatus_Fail_NotAdmin() {
		// given
		Long memberId = 1L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(adminMember));
		when(adminMember.getRole()).thenReturn(SystemRole.MEMBER);

		MemberStatusChange.Request request = new MemberStatusChange.Request(2L, MemberStatus.BANNED);

		// when & then
		assertThatThrownBy(() -> memberStatusChangeService.changeStatus(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.ADMIN_ONLY_ACTION);

		verify(memberCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 대상 회원에 대해 DomainException이 발생한다")
	void changeStatus_Fail_TargetMemberNotFound() {
		// given
		Long adminId = 1L;
		Long targetMemberId = 999L;
		when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(adminMember));
		when(memberQueryRepository.findById(targetMemberId)).thenReturn(Optional.empty());
		when(adminMember.getRole()).thenReturn(SystemRole.ADMIN);

		MemberStatusChange.Request request = new MemberStatusChange.Request(targetMemberId, MemberStatus.BANNED);

		// when & then
		assertThatThrownBy(() -> memberStatusChangeService.changeStatus(adminId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(memberCommandRepository, never()).save(any());
	}
}

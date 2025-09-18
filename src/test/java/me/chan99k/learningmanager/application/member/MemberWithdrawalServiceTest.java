package me.chan99k.learningmanager.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
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
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.member.Account;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalServiceTest {

	@InjectMocks
	private MemberWithdrawalService memberWithdrawalService;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@Mock
	private MemberCommandRepository memberCommandRepository;

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@Mock
	private Member member;

	@Mock
	private Account account;

	@Mock
	private Course course;

	@Test
	@DisplayName("[Success] 회원 탈퇴에 성공한다")
	void withdrawal_Success() {
		Long memberId = 1L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(memberId));
			when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(courseQueryRepository.findManagedCoursesByMemberId(memberId)).thenReturn(Collections.emptyList());
			when(member.getAccounts()).thenReturn(List.of(account));
			when(account.getId()).thenReturn(1L);

			memberWithdrawalService.withdrawal();

			verify(member).withdraw();
			verify(member).deactivateAccount(1L);
			verify(memberCommandRepository).save(member);
		}
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 AuthenticationException이 발생한다")
	void withdrawal_Fail_Unauthenticated() {
		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			assertThatThrownBy(() -> memberWithdrawalService.withdrawal())
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);

			verify(memberQueryRepository, never()).findById(anyLong());
			verify(memberCommandRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 회원은 DomainException이 발생한다")
	void withdrawal_Fail_MemberNotFound() {
		Long memberId = 999L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(memberId));
			when(memberQueryRepository.findById(memberId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> memberWithdrawalService.withdrawal())
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

			verify(memberCommandRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("[Failure] 스터디장으로 활동 중인 과정이 있으면 IllegalStateException이 발생한다")
	void withdrawal_Fail_HasManagedCourses() {
		Long memberId = 1L;

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(memberId));
			when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(courseQueryRepository.findManagedCoursesByMemberId(memberId)).thenReturn(List.of(course));

			assertThatThrownBy(() -> memberWithdrawalService.withdrawal())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("[System] 스터디장 권한을 다른 멤버에게 위임한 후 탈퇴할 수 있습니다.");

			verify(member, never()).withdraw();
			verify(memberCommandRepository, never()).save(any());
		}
	}
}
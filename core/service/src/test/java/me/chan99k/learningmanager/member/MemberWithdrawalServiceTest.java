package me.chan99k.learningmanager.member;

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
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;

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
		// given
		Long memberId = 1L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(courseQueryRepository.findManagedCoursesByMemberId(memberId)).thenReturn(Collections.emptyList());
		when(member.getAccounts()).thenReturn(List.of(account));
		when(account.getId()).thenReturn(1L);

		// when
		memberWithdrawalService.withdrawal(memberId);

		// then
		verify(member).withdraw();
		verify(member).deactivateAccount(1L);
		verify(memberCommandRepository).save(member);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 회원은 DomainException이 발생한다")
	void withdrawal_Fail_MemberNotFound() {
		// given
		Long memberId = 999L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> memberWithdrawalService.withdrawal(memberId))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);

		verify(memberCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 스터디장으로 활동 중인 과정이 있으면 IllegalStateException이 발생한다")
	void withdrawal_Fail_HasManagedCourses() {
		// given
		Long memberId = 1L;
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(courseQueryRepository.findManagedCoursesByMemberId(memberId)).thenReturn(List.of(course));

		// when & then
		assertThatThrownBy(() -> memberWithdrawalService.withdrawal(memberId))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("[System] 스터디장 권한을 다른 멤버에게 위임한 후 탈퇴할 수 있습니다.");

		verify(member, never()).withdraw();
		verify(memberCommandRepository, never()).save(any());
	}
}

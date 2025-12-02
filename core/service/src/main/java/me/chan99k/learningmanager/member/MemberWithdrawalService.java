package me.chan99k.learningmanager.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class MemberWithdrawalService implements MemberWithdrawal {

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final CourseQueryRepository courseQueryRepository;
	private final UserContext userContext;

	public MemberWithdrawalService(
		MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository,
		CourseQueryRepository courseQueryRepository,
		UserContext userContext) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.courseQueryRepository = courseQueryRepository;
		this.userContext = userContext;
	}

	@Override
	public void withdrawal() {
		Long memberId = userContext.getCurrentMemberId();

		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		List<Course> managedCourses = courseQueryRepository.findManagedCoursesByMemberId(
			memberId); // 관리 권한을 가진 채로 탈퇴 불가

		if (!managedCourses.isEmpty()) {
			throw new IllegalStateException(MemberProblemCode.CANNOT_WITHDRAW_WHEN_YOU_ARE_MANAGER.getMessage());
		}

		member.withdraw();

		for (Account account : member.getAccounts()) {
			member.deactivateAccount(account.getId());
		}

		memberCommandRepository.save(member);
	}
}
package me.chan99k.learningmanager.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class MemberWithdrawalService implements MemberWithdrawal {

	private final MemberQueryRepository memberQueryRepository;
	private final MemberCommandRepository memberCommandRepository;
	private final CourseQueryRepository courseQueryRepository;

	public MemberWithdrawalService(
		MemberQueryRepository memberQueryRepository,
		MemberCommandRepository memberCommandRepository,
		CourseQueryRepository courseQueryRepository) {
		this.memberQueryRepository = memberQueryRepository;
		this.memberCommandRepository = memberCommandRepository;
		this.courseQueryRepository = courseQueryRepository;
	}

	@Override
	public void withdrawal(Long requestedBy) {
		Member member = memberQueryRepository.findById(requestedBy)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		List<Course> managedCourses = courseQueryRepository.findManagedCoursesByMemberId(
			requestedBy); // 관리 권한을 가진 채로 탈퇴 불가

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
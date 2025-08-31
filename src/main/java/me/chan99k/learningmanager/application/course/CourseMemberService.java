package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CourseMemberAddition;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseProblemCode;
import me.chan99k.learningmanager.domain.course.CourseRole;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@Service
@Transactional
public class CourseMemberService implements CourseMemberAddition {
	private final CourseQueryRepository queryRepository;
	private final CourseCommandRepository commandRepository;
	private final MemberQueryRepository memberQueryRepository;

	public CourseMemberService(CourseQueryRepository queryRepository, CourseCommandRepository commandRepository,
		MemberQueryRepository memberQueryRepository) {
		this.queryRepository = queryRepository;
		this.commandRepository = commandRepository;
		this.memberQueryRepository = memberQueryRepository;
	}

	@Override
	public CourseMemberAddition.Response addMemberToCourse(Long courseId, CourseMemberAddition.Request request) {
		Long managerId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		Course course = queryRepository.findById(courseId)
			.orElseThrow(() -> new DomainException(CourseProblemCode.COURSE_NOT_FOUND));

		// 권한 확인: 요청자가 해당 과정의 매니저인지 확인
		boolean isManager = course.getCourseMemberList().stream()
			.anyMatch(cm -> cm.getMemberId().equals(managerId) && cm.getCourseRole() == CourseRole.MANAGER);
		if (!isManager) {
			throw new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED);
		}

		Member memberToAdd = memberQueryRepository.findByEmail(Email.of(request.email()))
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		course.addMember(memberToAdd.getId(), request.role());
		commandRepository.save(course);

		return new CourseMemberAddition.Response();
	}
}

package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CourseCreation;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseRole;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.SystemRole;

@Service
@Transactional
public class CourseCreationService implements CourseCreation {
	private final CourseCommandRepository commandRepository;
	private final MemberQueryRepository memberQueryRepository;

	public CourseCreationService(CourseCommandRepository commandRepository,
		MemberQueryRepository memberQueryRepository) {
		this.commandRepository = commandRepository;
		this.memberQueryRepository = memberQueryRepository;
	}

	@Override
	public Response createCourse(Request request) {
		Long currentMemberId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthException(
				AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		Member member = memberQueryRepository.findById(currentMemberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		if (!member.getRole().equals(SystemRole.ADMIN)) { // 인가 - 권한 확인
			throw new AuthException(AuthProblemCode.AUTHORIZATION_REQUIRED);
		}

		Course newCourse = Course.create(request.title(), request.description());
		newCourse.addMember(currentMemberId, CourseRole.MANAGER);

		Course savedCourse = commandRepository.create(newCourse);

		return new CourseCreation.Response(savedCourse.getId());
	}
}

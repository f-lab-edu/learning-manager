package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.SystemRole;

@Service
@Transactional
public class CourseCreationService implements CourseCreation {
	private final CourseCommandRepository commandRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final UserContext userContext;

	public CourseCreationService(CourseCommandRepository commandRepository,
		MemberQueryRepository memberQueryRepository, UserContext userContext) {
		this.commandRepository = commandRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.userContext = userContext;
	}

	@Override
	public Response createCourse(Request request) {
		Long currentMemberId = userContext.getCurrentMemberId();

		Member member = memberQueryRepository.findById(currentMemberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		if (!member.getRole().equals(SystemRole.ADMIN)) { // 인가 - 권한 확인
			throw new DomainException(CourseProblemCode.ADMIN_ONLY_COURSE_CREATION);
		}

		Course newCourse = Course.create(request.title(), request.description());
		newCourse.addMember(currentMemberId, CourseRole.MANAGER);

		Course savedCourse = commandRepository.create(newCourse);

		return new CourseCreation.Response(savedCourse.getId());
	}
}

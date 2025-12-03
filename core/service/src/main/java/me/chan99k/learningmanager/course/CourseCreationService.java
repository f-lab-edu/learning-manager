package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public CourseCreationService(CourseCommandRepository commandRepository,
		MemberQueryRepository memberQueryRepository) {
		this.commandRepository = commandRepository;
		this.memberQueryRepository = memberQueryRepository;
	}

	@Override
	public Response createCourse(Long requestedBy, Request request) {
		Member member = memberQueryRepository.findById(requestedBy)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		if (!member.getRole().equals(SystemRole.ADMIN)) { // 인가 - 권한 확인
			throw new DomainException(CourseProblemCode.ADMIN_ONLY_COURSE_CREATION);
		}

		Course newCourse = Course.create(request.title(), request.description());
		newCourse.addMember(requestedBy, CourseRole.MANAGER);

		Course savedCourse = commandRepository.create(newCourse);

		return new CourseCreation.Response(savedCourse.getId());
	}
}

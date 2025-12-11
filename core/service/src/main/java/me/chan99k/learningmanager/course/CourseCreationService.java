package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.SystemRole;

@Service
@Transactional
public class CourseCreationService implements CourseCreation {

	private final CourseCommandRepository commandRepository;
	private final SystemAuthorizationPort systemAuthorizationPort;

	public CourseCreationService(
		CourseCommandRepository commandRepository,
		SystemAuthorizationPort systemAuthorizationPort
	) {
		this.commandRepository = commandRepository;
		this.systemAuthorizationPort = systemAuthorizationPort;
	}

	@Override
	public Response createCourse(Long requestedBy, Request request) {
		if (!systemAuthorizationPort.hasRole(requestedBy, SystemRole.ADMIN)) {
			throw new DomainException(CourseProblemCode.ADMIN_ONLY_COURSE_CREATION);
		}

		Course newCourse = Course.create(request.title(), request.description());
		newCourse.addMember(requestedBy, CourseRole.MANAGER);

		Course savedCourse = commandRepository.create(newCourse);

		return new CourseCreation.Response(savedCourse.getId());
	}

}

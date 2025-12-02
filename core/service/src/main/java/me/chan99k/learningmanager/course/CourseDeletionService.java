package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class CourseDeletionService implements CourseDeletion {
	private final CourseCommandRepository commandRepository;
	private final CourseQueryRepository queryRepository;
	private final UserContext userContext;

	public CourseDeletionService(CourseCommandRepository commandRepository, CourseQueryRepository queryRepository,
		UserContext userContext) {
		this.commandRepository = commandRepository;
		this.queryRepository = queryRepository;
		this.userContext = userContext;
	}

	@Override
	public void deleteCourse(Long courseId) {
		Course course = authenticatedAndAuthorizedCourseByManager(courseId);

		commandRepository.delete(course);
	}

	/**
	 * Authenticates the current member and checks if they are the manager of the given course.
	 *
	 * @param courseId the course to check
	 * @throws DomainException if the member is not the course manager
	 */
	private Course authenticatedAndAuthorizedCourseByManager(Long courseId) {
		Long managerId = userContext.getCurrentMemberId();

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
	}
}
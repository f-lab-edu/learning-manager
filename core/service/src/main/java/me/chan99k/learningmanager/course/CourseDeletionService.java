package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class CourseDeletionService implements CourseDeletion {
	private final CourseCommandRepository commandRepository;
	private final CourseQueryRepository queryRepository;

	public CourseDeletionService(CourseCommandRepository commandRepository, CourseQueryRepository queryRepository) {
		this.commandRepository = commandRepository;
		this.queryRepository = queryRepository;
	}

	@Override
	public void deleteCourse(Long requestedBy, Long courseId) {
		Course course = authenticatedAndAuthorizedCourseByManager(requestedBy, courseId);

		commandRepository.delete(course);
	}

	/**
	 * Authenticates the current member and checks if they are the manager of the given course.
	 *
	 * @param requestedBy the member ID of the requester
	 * @param courseId the course to check
	 * @throws DomainException if the member is not the course manager
	 */
	private Course authenticatedAndAuthorizedCourseByManager(Long requestedBy, Long courseId) {
		return queryRepository.findManagedCourseById(courseId, requestedBy)
			.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
	}
}
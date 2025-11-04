package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.course.provides.CourseDeletion;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;

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
	 * @throws AuthorizationException  if the member is not the course manager
	 */
	private Course authenticatedAndAuthorizedCourseByManager(Long courseId) {
		Long managerId = userContext.getCurrentMemberId();

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
	}
}
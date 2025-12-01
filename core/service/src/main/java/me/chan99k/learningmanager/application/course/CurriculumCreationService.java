package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.auth.requires.UserContext;
import me.chan99k.learningmanager.application.course.provides.CurriculumCreation;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthorizationException;

@Service
@Transactional
public class CurriculumCreationService implements CurriculumCreation {
	private final CourseCommandRepository commandRepository;
	private final CourseQueryRepository queryRepository;
	private final UserContext userContext;

	public CurriculumCreationService(CourseCommandRepository commandRepository, CourseQueryRepository queryRepository,
		UserContext userContext) {
		this.commandRepository = commandRepository;
		this.queryRepository = queryRepository;
		this.userContext = userContext;
	}

	@Override
	public Response createCurriculum(Long courseId, Request request) {
		Course course = authenticateAndAuthorizeManager(courseId);

		Curriculum newCurriculum = course.addCurriculum(request.title(), request.description());

		commandRepository.save(course);

		// 트랜잭션 내에서 newCurriculum은 영속화 -> ID를 가짐
		return new Response(newCurriculum.getId(), newCurriculum.getTitle());
	}

	/**
	 * Authenticates the current member and checks if they are the manager of the given course.
	 *
	 * @param courseId the course to check
	 * @throws AuthorizationException  if the member is not the course manager
	 */
	private Course authenticateAndAuthorizeManager(Long courseId) {
		Long managerId = userContext.getCurrentMemberId();

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
	}
}

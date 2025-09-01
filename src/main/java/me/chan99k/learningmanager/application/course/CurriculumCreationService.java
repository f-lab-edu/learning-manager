package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CurriculumCreation;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;

@Service
@Transactional
public class CurriculumCreationService implements CurriculumCreation {
	private final CourseCommandRepository commandRepository;
	private final CourseQueryRepository queryRepository;

	public CurriculumCreationService(CourseCommandRepository commandRepository, CourseQueryRepository queryRepository) {
		this.commandRepository = commandRepository;
		this.queryRepository = queryRepository;
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
	 * @throws AuthenticationException if authentication context is not found
	 * @throws AuthorizationException  if the member is not the course manager
	 */
	private Course authenticateAndAuthorizeManager(Long courseId) {
		Long managerId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
	}
}

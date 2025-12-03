package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

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
	public Response createCurriculum(Long requestedBy, Long courseId, Request request) {
		Course course = authenticateAndAuthorizeManager(requestedBy, courseId);

		Curriculum newCurriculum = course.addCurriculum(request.title(), request.description());

		commandRepository.save(course);

		// 트랜잭션 내에서 newCurriculum은 영속화 -> ID를 가짐
		return new Response(newCurriculum.getId(), newCurriculum.getTitle());
	}

	/**
	 * Authenticates the current member and checks if they are the manager of the given course.
	 *
	 * @param requestedBy the member ID of the requester
	 * @param courseId the course to check
	 * @throws DomainException if the member is not the course manager
	 */
	private Course authenticateAndAuthorizeManager(Long requestedBy, Long courseId) {
		return queryRepository.findManagedCourseById(courseId, requestedBy)
			.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
	}
}

package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class CurriculumDeletionService implements CurriculumDeletion {
	private final CourseCommandRepository commandRepository;
	private final CourseQueryRepository queryRepository;
	private final UserContext userContext;

	public CurriculumDeletionService(CourseCommandRepository commandRepository, CourseQueryRepository queryRepository,
		UserContext userContext) {
		this.commandRepository = commandRepository;
		this.queryRepository = queryRepository;
		this.userContext = userContext;
	}

	@Override
	public void deleteCurriculum(Long courseId, Long curriculumId) {
		Course course = authenticateAndAuthorizeManager(courseId);

		Curriculum curriculumToDelete = course.findCurriculumById(curriculumId);
		course.removeCurriculum(curriculumToDelete);

		commandRepository.save(course);
	}

	private Course authenticateAndAuthorizeManager(Long courseId) {
		Long managerId = userContext.getCurrentMemberId();

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
	}
}
package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class CurriculumDeletionService implements CurriculumDeletion {
	private final CourseCommandRepository commandRepository;
	private final CourseQueryRepository queryRepository;

	public CurriculumDeletionService(CourseCommandRepository commandRepository, CourseQueryRepository queryRepository) {
		this.commandRepository = commandRepository;
		this.queryRepository = queryRepository;
	}

	@Override
	public void deleteCurriculum(Long requestedBy, Long courseId, Long curriculumId) {
		Course course = authenticateAndAuthorizeManager(requestedBy, courseId);

		Curriculum curriculumToDelete = course.findCurriculumById(curriculumId);
		course.removeCurriculum(curriculumToDelete);

		commandRepository.save(course);
	}

	private Course authenticateAndAuthorizeManager(Long requestedBy, Long courseId) {
		return queryRepository.findManagedCourseById(courseId, requestedBy)
			.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
	}
}
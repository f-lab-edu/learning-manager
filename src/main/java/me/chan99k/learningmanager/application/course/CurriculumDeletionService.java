package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.course.provides.CurriculumDeletion;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;

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
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
	}
}
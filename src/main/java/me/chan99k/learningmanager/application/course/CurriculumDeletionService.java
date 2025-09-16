package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CurriculumDeletion;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;

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
	public void deleteCurriculum(Long courseId, Long curriculumId) {
		Course course = authenticateAndAuthorizeManager(courseId);

		Curriculum curriculumToDelete = course.findCurriculumById(curriculumId);
		course.removeCurriculum(curriculumToDelete);

		commandRepository.save(course);
	}

	private Course authenticateAndAuthorizeManager(Long courseId) {
		Long managerId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
	}
}
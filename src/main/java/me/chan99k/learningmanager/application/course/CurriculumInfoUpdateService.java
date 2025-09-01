package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CurriculumInfoUpdate;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;

@Service
@Transactional
public class CurriculumInfoUpdateService implements CurriculumInfoUpdate {

	private final CourseQueryRepository queryRepository;
	private final CourseCommandRepository commandRepository;

	public CurriculumInfoUpdateService(
		CourseQueryRepository queryRepository,
		CourseCommandRepository commandRepository) {
		this.queryRepository = queryRepository;
		this.commandRepository = commandRepository;
	}

	@Override
	public void updateCurriculumInfo(Long courseId, Long curriculumId, CurriculumInfoUpdate.Request request) {
		// 업데이트할 필드가 하나도 없으면 예외 발생
		if (request.title() == null && request.description() == null) {
			throw new IllegalArgumentException("제목 또는 설명 중 하나 이상을 입력해주세요");
		}

		Course course = authenticatedAndAuthorizedCourseByManager(courseId);

		Curriculum curriculum = course.findCurriculumById(curriculumId);

		if (request.title() != null) {
			curriculum.updateTitle(request.title());
		}

		if (request.description() != null) {
			curriculum.updateDescription(request.description());
		}

		commandRepository.save(course);
	}

	private Course authenticatedAndAuthorizedCourseByManager(Long courseId) {
		Long managerId = AuthenticationContextHolder.getCurrentMemberId()
			.orElseThrow(() -> new AuthenticationException(AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND));

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
	}
}
package me.chan99k.learningmanager.application.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.course.provides.CourseInfoUpdate;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.course.Course;

@Service
@Transactional
public class CourseInfoUpdateService implements CourseInfoUpdate {

	private final CourseQueryRepository queryRepository;
	private final CourseCommandRepository commandRepository;
	private final UserContext userContext;

	public CourseInfoUpdateService(
		CourseQueryRepository queryRepository,
		CourseCommandRepository commandRepository, UserContext userContext) {
		this.queryRepository = queryRepository;
		this.commandRepository = commandRepository;
		this.userContext = userContext;
	}

	@Override
	public void updateCourseInfo(Long courseId, CourseInfoUpdate.Request request) {
		if (request.title() == null && request.description() == null) {
			throw new IllegalArgumentException("제목 또는 설명 중 하나 이상을 입력해주세요");
		}

		Course course = authenticatedAndAuthorizedCourseByManager(courseId);

		if (request.title() != null) {
			course.updateTitle(request.title());
		}

		if (request.description() != null) {
			course.updateDescription(request.description());
		}

		commandRepository.save(course);
	}

	private Course authenticatedAndAuthorizedCourseByManager(Long courseId) {
		Long managerId = userContext.getCurrentMemberId();

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
	}
}
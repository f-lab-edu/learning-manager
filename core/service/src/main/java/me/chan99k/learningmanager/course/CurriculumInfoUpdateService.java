package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class CurriculumInfoUpdateService implements CurriculumInfoUpdate {

	private final CourseQueryRepository queryRepository;
	private final CourseCommandRepository commandRepository;
	private final UserContext userContext;

	public CurriculumInfoUpdateService(
		CourseQueryRepository queryRepository,
		CourseCommandRepository commandRepository,
		UserContext userContext) {
		this.queryRepository = queryRepository;
		this.commandRepository = commandRepository;
		this.userContext = userContext;
	}

	@Override
	public void updateCurriculumInfo(Long courseId, Long curriculumId, CurriculumInfoUpdate.Request request) {
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
		Long managerId = userContext.getCurrentMemberId();

		return queryRepository.findManagedCourseById(courseId, managerId)
			.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
	}
}
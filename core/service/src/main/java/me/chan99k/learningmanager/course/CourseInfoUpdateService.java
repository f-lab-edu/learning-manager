package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class CourseInfoUpdateService implements CourseInfoUpdate {

	private final CourseQueryRepository queryRepository;
	private final CourseCommandRepository commandRepository;

	public CourseInfoUpdateService(
		CourseQueryRepository queryRepository,
		CourseCommandRepository commandRepository) {
		this.queryRepository = queryRepository;
		this.commandRepository = commandRepository;
	}

	@Override
	public void updateCourseInfo(Long requestedBy, Long courseId, CourseInfoUpdate.Request request) {
		if (request.title() == null && request.description() == null) {
			throw new IllegalArgumentException("제목 또는 설명 중 하나 이상을 입력해주세요");
		}

		Course course = authenticatedAndAuthorizedCourseByManager(requestedBy, courseId);

		if (request.title() != null) {
			course.updateTitle(request.title());
		}

		if (request.description() != null) {
			course.updateDescription(request.description());
		}

		commandRepository.save(course);
	}

	private Course authenticatedAndAuthorizedCourseByManager(Long requestedBy, Long courseId) {
		return queryRepository.findManagedCourseById(courseId, requestedBy)
			.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
	}
}
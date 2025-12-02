package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional(readOnly = true)
public class CourseDetailService implements CourseDetailRetrieval {

	private final CourseQueryRepository courseQueryRepository;

	public CourseDetailService(CourseQueryRepository courseQueryRepository) {
		this.courseQueryRepository = courseQueryRepository;
	}

	@Override
	public CourseDetailResponse getCourseDetail(Long courseId) {
		CourseDetailInfo courseDetail = courseQueryRepository.findCourseDetailById(courseId)
			.orElseThrow(() -> new DomainException(CourseProblemCode.COURSE_NOT_FOUND));

		return new CourseDetailResponse(courseDetail);
	}

	@Override
	public PageResult<CourseMemberInfo> getCourseMembers(Long courseId, PageRequest pageRequest) {
		courseQueryRepository.findById(courseId)
			.orElseThrow(() -> new DomainException(CourseProblemCode.COURSE_NOT_FOUND));

		return courseQueryRepository.findCourseMembersByCourseId(courseId, pageRequest);
	}
}
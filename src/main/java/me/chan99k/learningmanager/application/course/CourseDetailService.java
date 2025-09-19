package me.chan99k.learningmanager.application.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.course.provides.CourseDetailRetrieval;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.CourseProblemCode;

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
	public Page<CourseMemberInfo> getCourseMembers(Long courseId, Pageable pageable) {
		courseQueryRepository.findById(courseId)
			.orElseThrow(() -> new DomainException(CourseProblemCode.COURSE_NOT_FOUND));

		return courseQueryRepository.findCourseMembersByCourseId(courseId, pageable);
	}
}
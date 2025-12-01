package me.chan99k.learningmanager.course;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.course.CourseDetailInfo;
import me.chan99k.learningmanager.application.course.CourseMemberInfo;
import me.chan99k.learningmanager.application.course.CourseQueryRepository;
import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.common.SortOrder;
import me.chan99k.learningmanager.course.mapper.CourseMapper;
import me.chan99k.learningmanager.domain.course.Course;

@Repository
public class CourseQueryAdapter implements CourseQueryRepository {

	private final JpaCourseRepository jpaCourseRepository;

	public CourseQueryAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public Optional<Course> findById(Long courseId) {
		return jpaCourseRepository.findById(courseId)
			.map(CourseMapper::toDomain);
	}

	@Override
	public Optional<Course> findByTitle(String title) {
		return jpaCourseRepository.findByTitle(title)
			.map(CourseMapper::toDomain);
	}

	@Override
	public Optional<Course> findManagedCourseById(Long courseId, Long memberId) {
		return jpaCourseRepository.findManagedCourseById(courseId, memberId)
			.map(CourseMapper::toDomain);
	}

	@Override
	public List<Course> findManagedCoursesByMemberId(Long memberId) {
		return jpaCourseRepository.findManagedCoursesByMemberId(memberId).stream()
			.map(CourseMapper::toDomain)
			.toList();
	}

	@Override
	public Optional<CourseDetailInfo> findCourseDetailById(Long courseId) {
		return jpaCourseRepository.findCourseBasicDetailsById(courseId);
	}

	@Override
	public PageResult<CourseMemberInfo> findCourseMembersByCourseId(Long courseId, PageRequest pageRequest) {
		Pageable pageable = toSpringPageable(pageRequest);
		Page<CourseMemberInfo> page = jpaCourseRepository.findCourseMembersByCourseId(courseId, pageable);
		return PageResult.of(page.getContent(), pageRequest, page.getTotalElements());
	}

	private Pageable toSpringPageable(PageRequest pageRequest) {
		if (pageRequest.hasSort()) {
			Sort.Direction direction = pageRequest.sortOrder() == SortOrder.DESC
				? Sort.Direction.DESC : Sort.Direction.ASC;
			return org.springframework.data.domain.PageRequest.of(
				pageRequest.page(), pageRequest.size(), Sort.by(direction, pageRequest.sortBy()));
		}
		return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());
	}
}

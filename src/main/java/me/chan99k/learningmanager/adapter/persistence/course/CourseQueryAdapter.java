package me.chan99k.learningmanager.adapter.persistence.course;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.course.CourseDetailInfo;
import me.chan99k.learningmanager.application.course.CourseMemberInfo;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;

@Repository
public class CourseQueryAdapter implements CourseQueryRepository {

	private final JpaCourseRepository jpaCourseRepository;

	public CourseQueryAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public Optional<Course> findById(Long courseId) {
		return jpaCourseRepository.findById(courseId);
	}

	@Override
	public Optional<Course> findByTitle(String title) {
		return jpaCourseRepository.findByTitle(title);
	}

	@Override
	public Optional<Course> findManagedCourseById(Long courseId, Long memberId) {
		return jpaCourseRepository.findManagedCourseById(courseId, memberId);
	}

	@Override
	public List<Course> findManagedCoursesByMemberId(Long memberId) {
		return jpaCourseRepository.findManagedCoursesByMemberId(memberId);
	}

	@Override
	public Optional<CourseDetailInfo> findCourseDetailById(Long courseId) {
		Optional<Object[]> basicDetails = jpaCourseRepository.findCourseBasicDetailsById(courseId);
		if (basicDetails.isEmpty()) {
			return Optional.empty();
		}

		Object[] row = basicDetails.get();
		Long id = ((Number)row[0]).longValue();
		String title = (String)row[1];
		String description = (String)row[2];
		Instant createdAt = (Instant)row[3];
		int totalMembers = ((Number)row[4]).intValue();
		int totalCurricula = ((Number)row[5]).intValue();
		int totalSessions = ((Number)row[6]).intValue();

		CourseDetailInfo courseDetailInfo = new CourseDetailInfo(
			id, title, description, createdAt,
			totalMembers, totalCurricula, totalSessions
		);

		return Optional.of(courseDetailInfo);
	}

	@Override
	public Page<CourseMemberInfo> findCourseMembersByCourseId(Long courseId, Pageable pageable) {
		return jpaCourseRepository.findCourseMembersByCourseId(courseId, pageable);
	}
}

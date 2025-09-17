package me.chan99k.learningmanager.adapter.persistence.course;

import java.util.List;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.member.requires.CourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;

@Repository
public class MemberCourseQueryAdapter implements CourseQueryRepository {

	private final JpaCourseRepository jpaCourseRepository;

	public MemberCourseQueryAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public List<Course> findManagedCoursesByMemberId(Long memberId) {
		return jpaCourseRepository.findManagedCoursesByMemberId(memberId);
	}
}
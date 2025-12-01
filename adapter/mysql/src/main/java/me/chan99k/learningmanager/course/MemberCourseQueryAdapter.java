package me.chan99k.learningmanager.course;

import java.util.List;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.member.CourseParticipationInfo;
import me.chan99k.learningmanager.application.member.MemberCourseQueryRepository;
import me.chan99k.learningmanager.course.mapper.CourseMapper;
import me.chan99k.learningmanager.domain.course.Course;

@Repository
public class MemberCourseQueryAdapter implements MemberCourseQueryRepository {

	private final JpaCourseRepository jpaCourseRepository;

	public MemberCourseQueryAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public List<Course> findManagedCoursesByMemberId(Long memberId) {
		return jpaCourseRepository.findManagedCoursesByMemberId(memberId).stream()
			.map(CourseMapper::toDomain)
			.toList();
	}

	@Override
	public List<Course> findParticipatingCoursesByMemberId(Long memberId) {
		return jpaCourseRepository.findParticipatingCoursesByMemberId(memberId).stream()
			.map(CourseMapper::toDomain)
			.toList();
	}

	@Override
	public List<CourseParticipationInfo> findParticipatingCoursesWithRoleByMemberId(Long memberId) {
		return jpaCourseRepository.findParticipatingCoursesWithRoleByMemberId(memberId);
	}
}
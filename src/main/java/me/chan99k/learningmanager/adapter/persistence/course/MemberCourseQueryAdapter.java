package me.chan99k.learningmanager.adapter.persistence.course;

import java.util.List;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.member.requires.MemberCourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;

@Repository
public class MemberCourseQueryAdapter implements MemberCourseQueryRepository {

	private final JpaCourseRepository jpaCourseRepository;

	public MemberCourseQueryAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public List<Course> findManagedCoursesByMemberId(Long memberId) {
		return jpaCourseRepository.findManagedCoursesByMemberId(memberId);
	}

	@Override
	public List<Course> findParticipatingCoursesByMemberId(Long memberId) {
		return jpaCourseRepository.findParticipatingCoursesByMemberId(memberId);
	}
}
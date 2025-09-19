package me.chan99k.learningmanager.application.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.member.requires.MemberCourseQueryRepository;

@Service
@Transactional(readOnly = true)
public class MemberCourseParticipationService {

	private final MemberCourseQueryRepository memberCourseQueryRepository;

	public MemberCourseParticipationService(MemberCourseQueryRepository memberCourseQueryRepository) {
		this.memberCourseQueryRepository = memberCourseQueryRepository;
	}

	public ParticipatingCoursesResponse getParticipatingCourses(Long memberId) {
		List<CourseParticipationInfo> courseInfos = memberCourseQueryRepository.findParticipatingCoursesWithRoleByMemberId(
			memberId);
		return new ParticipatingCoursesResponse(courseInfos);
	}

	public record ParticipatingCoursesResponse(
		List<CourseParticipationInfo> courses
	) {
	}
}
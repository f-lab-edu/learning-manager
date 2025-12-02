package me.chan99k.learningmanager.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberCourseParticipationService implements MemberCourseParticipation {

	private final MemberCourseQueryRepository memberCourseQueryRepository;

	public MemberCourseParticipationService(MemberCourseQueryRepository memberCourseQueryRepository) {
		this.memberCourseQueryRepository = memberCourseQueryRepository;
	}

	@Override
	public Response getParticipatingCourses(Long memberId) {
		List<CourseParticipationInfo> courseInfos = memberCourseQueryRepository.findParticipatingCoursesWithRoleByMemberId(
			memberId);
		return new Response(courseInfos);
	}
}
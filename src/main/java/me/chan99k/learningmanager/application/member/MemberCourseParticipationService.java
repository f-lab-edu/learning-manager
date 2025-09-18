package me.chan99k.learningmanager.application.member;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.member.requires.MemberCourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseMember;

@Service
@Transactional(readOnly = true)
public class MemberCourseParticipationService {

	private final MemberCourseQueryRepository memberCourseQueryRepository;

	public MemberCourseParticipationService(MemberCourseQueryRepository memberCourseQueryRepository) {
		this.memberCourseQueryRepository = memberCourseQueryRepository;
	}

	public ParticipatingCoursesResponse getParticipatingCourses(Long memberId) {
		List<Course> participatingCourses = memberCourseQueryRepository.findParticipatingCoursesByMemberId(memberId);

		List<CourseParticipationInfo> courseInfos = participatingCourses.stream()
			.map(course -> {
				CourseMember memberInfo = course.getCourseMemberList().stream()
					.filter(cm -> cm.getMemberId().equals(memberId))
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다"));

				return new CourseParticipationInfo(
					course.getId(),
					course.getTitle(),
					course.getDescription(),
					memberInfo.getCourseRole()
				);
			})
			.collect(Collectors.toList());

		return new ParticipatingCoursesResponse(courseInfos);
	}

	public record ParticipatingCoursesResponse(
		List<CourseParticipationInfo> courses
	) {
	}

	public record CourseParticipationInfo(
		Long courseId,
		String title,
		String description,
		me.chan99k.learningmanager.domain.course.CourseRole role
	) {
	}
}
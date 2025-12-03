package me.chan99k.learningmanager.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.member.CourseParticipationInfo;
import me.chan99k.learningmanager.member.MemberCourseParticipation;
import me.chan99k.learningmanager.member.MemberCourseParticipationService;
import me.chan99k.learningmanager.member.MemberCourseQueryRepository;

@ExtendWith(MockitoExtension.class)
class MemberCourseParticipationServiceTest {

	@Mock
	private MemberCourseQueryRepository memberCourseQueryRepository;

	private MemberCourseParticipationService service;

	@BeforeEach
	void setUp() {
		service = new MemberCourseParticipationService(memberCourseQueryRepository);
	}

	@Test
	@DisplayName("멤버가 참여한 모든 과정을 역할과 함께 조회한다")
	void getParticipatingCourses_success() {
		// given
		Long memberId = 1L;

		CourseParticipationInfo courseInfo1 = new CourseParticipationInfo(
			1L, "Java 기초", "Java 프로그래밍 기초 과정", CourseRole.MANAGER
		);
		CourseParticipationInfo courseInfo2 = new CourseParticipationInfo(
			2L, "Spring Boot", "Spring Boot 웹 개발 과정", CourseRole.MENTEE
		);

		List<CourseParticipationInfo> mockCourseInfos = Arrays.asList(courseInfo1, courseInfo2);
		when(memberCourseQueryRepository.findParticipatingCoursesWithRoleByMemberId(memberId))
			.thenReturn(mockCourseInfos);

		// when
		MemberCourseParticipation.Response response = service.getParticipatingCourses(memberId);

		// then
		assertThat(response.courses()).hasSize(2);

		CourseParticipationInfo actualCourse1 = response.courses().get(0);
		assertThat(actualCourse1.title()).isEqualTo("Java 기초");
		assertThat(actualCourse1.description()).isEqualTo("Java 프로그래밍 기초 과정");
		assertThat(actualCourse1.role()).isEqualTo(CourseRole.MANAGER);

		CourseParticipationInfo actualCourse2 = response.courses().get(1);
		assertThat(actualCourse2.title()).isEqualTo("Spring Boot");
		assertThat(actualCourse2.description()).isEqualTo("Spring Boot 웹 개발 과정");
		assertThat(actualCourse2.role()).isEqualTo(CourseRole.MENTEE);

		verify(memberCourseQueryRepository).findParticipatingCoursesWithRoleByMemberId(memberId);
	}

	@Test
	@DisplayName("참여한 과정이 없는 경우 빈 리스트를 반환한다")
	void getParticipatingCourses_whenNoCourses() {
		// given
		Long memberId = 1L;
		when(memberCourseQueryRepository.findParticipatingCoursesWithRoleByMemberId(memberId))
			.thenReturn(Collections.emptyList());

		// when
		MemberCourseParticipation.Response response = service.getParticipatingCourses(memberId);

		// then
		assertThat(response.courses()).isEmpty();
		verify(memberCourseQueryRepository).findParticipatingCoursesWithRoleByMemberId(memberId);
	}

	@Test
	@DisplayName("멤버가 여러 역할로 참여한 과정들을 모두 조회한다")
	void getParticipatingCourses_withMultipleRoles() {
		// given
		Long memberId = 1L;

		CourseParticipationInfo courseInfo1 = new CourseParticipationInfo(
			1L, "Java 기초", "Java 프로그래밍 기초 과정", CourseRole.MANAGER
		);
		CourseParticipationInfo courseInfo2 = new CourseParticipationInfo(
			2L, "Spring Boot", "Spring Boot 웹 개발 과정", CourseRole.MENTOR
		);
		CourseParticipationInfo courseInfo3 = new CourseParticipationInfo(
			3L, "React", "React 프론트엔드 과정", CourseRole.MENTEE
		);

		List<CourseParticipationInfo> mockCourseInfos = Arrays.asList(courseInfo1, courseInfo2, courseInfo3);
		when(memberCourseQueryRepository.findParticipatingCoursesWithRoleByMemberId(memberId))
			.thenReturn(mockCourseInfos);

		// when
		MemberCourseParticipation.Response response = service.getParticipatingCourses(memberId);

		// then
		assertThat(response.courses()).hasSize(3);

		assertThat(response.courses())
			.extracting(CourseParticipationInfo::role)
			.containsExactly(CourseRole.MANAGER, CourseRole.MENTOR, CourseRole.MENTEE);

		verify(memberCourseQueryRepository).findParticipatingCoursesWithRoleByMemberId(memberId);
	}
}
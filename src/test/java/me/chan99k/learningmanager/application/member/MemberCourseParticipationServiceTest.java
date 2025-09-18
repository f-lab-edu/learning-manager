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

import me.chan99k.learningmanager.application.member.MemberCourseParticipationService.CourseParticipationInfo;
import me.chan99k.learningmanager.application.member.MemberCourseParticipationService.ParticipatingCoursesResponse;
import me.chan99k.learningmanager.application.member.requires.MemberCourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseRole;

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

		Course course1 = Course.create("Java 기초", "Java 프로그래밍 기초 과정");
		Course course2 = Course.create("Spring Boot", "Spring Boot 웹 개발 과정");

		course1.addMember(memberId, CourseRole.MANAGER);
		course2.addMember(memberId, CourseRole.MENTEE);

		List<Course> mockCourses = Arrays.asList(course1, course2);
		when(memberCourseQueryRepository.findParticipatingCoursesByMemberId(memberId))
			.thenReturn(mockCourses);

		// when
		ParticipatingCoursesResponse response = service.getParticipatingCourses(memberId);

		// then
		assertThat(response.courses()).hasSize(2);

		CourseParticipationInfo courseInfo1 = response.courses().get(0);
		assertThat(courseInfo1.title()).isEqualTo("Java 기초");
		assertThat(courseInfo1.description()).isEqualTo("Java 프로그래밍 기초 과정");
		assertThat(courseInfo1.role()).isEqualTo(CourseRole.MANAGER);

		CourseParticipationInfo courseInfo2 = response.courses().get(1);
		assertThat(courseInfo2.title()).isEqualTo("Spring Boot");
		assertThat(courseInfo2.description()).isEqualTo("Spring Boot 웹 개발 과정");
		assertThat(courseInfo2.role()).isEqualTo(CourseRole.MENTEE);

		verify(memberCourseQueryRepository).findParticipatingCoursesByMemberId(memberId);
	}

	@Test
	@DisplayName("참여한 과정이 없는 경우 빈 리스트를 반환한다")
	void getParticipatingCourses_whenNoCourses() {
		// given
		Long memberId = 1L;
		when(memberCourseQueryRepository.findParticipatingCoursesByMemberId(memberId))
			.thenReturn(Collections.emptyList());

		// when
		ParticipatingCoursesResponse response = service.getParticipatingCourses(memberId);

		// then
		assertThat(response.courses()).isEmpty();
		verify(memberCourseQueryRepository).findParticipatingCoursesByMemberId(memberId);
	}

	@Test
	@DisplayName("멤버가 여러 역할로 참여한 과정들을 모두 조회한다")
	void getParticipatingCourses_withMultipleRoles() {
		// given
		Long memberId = 1L;

		Course course1 = Course.create("Java 기초", "Java 프로그래밍 기초 과정");
		Course course2 = Course.create("Spring Boot", "Spring Boot 웹 개발 과정");
		Course course3 = Course.create("React", "React 프론트엔드 과정");

		course1.addMember(memberId, CourseRole.MANAGER);
		course2.addMember(memberId, CourseRole.MENTOR);
		course3.addMember(memberId, CourseRole.MENTEE);

		List<Course> mockCourses = Arrays.asList(course1, course2, course3);
		when(memberCourseQueryRepository.findParticipatingCoursesByMemberId(memberId))
			.thenReturn(mockCourses);

		// when
		ParticipatingCoursesResponse response = service.getParticipatingCourses(memberId);

		// then
		assertThat(response.courses()).hasSize(3);

		assertThat(response.courses())
			.extracting(CourseParticipationInfo::role)
			.containsExactly(CourseRole.MANAGER, CourseRole.MENTOR, CourseRole.MENTEE);

		verify(memberCourseQueryRepository).findParticipatingCoursesByMemberId(memberId);
	}
}
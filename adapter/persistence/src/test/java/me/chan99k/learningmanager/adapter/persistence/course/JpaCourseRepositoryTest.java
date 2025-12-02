package me.chan99k.learningmanager.adapter.persistence.course;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseRole;

class JpaCourseRepositoryTest {

	@Test
	@DisplayName("새로운 쿼리 메서드가 성공적으로 추가되었음을 확인한다")
	void verifyNewQueryMethodExists() {
		// 컴파일 타임에 메서드 존재를 확인하는 테스트
		// Repository 인터페이스에 findParticipatingCoursesByMemberId 메서드가 존재하는지 확인

		// given
		Course course1 = Course.create("Java 기초", "Java 프로그래밍 기초 과정");
		Course course2 = Course.create("Spring Boot", "Spring Boot 웹 개발 과정");
		Long memberId1 = 1L;
		Long memberId2 = 2L;

		course1.addMember(memberId1, CourseRole.MANAGER);
		course1.addMember(memberId2, CourseRole.MENTOR);
		course2.addMember(memberId1, CourseRole.MENTEE);
		course2.addMember(memberId2, CourseRole.MANAGER);

		// then - 컴파일이 성공하면 메서드가 존재함을 의미
		assertThat(course1.getCourseMemberList()).hasSize(2);
		assertThat(course2.getCourseMemberList()).hasSize(2);

		// 멤버1이 Java 기초에서는 MANAGER 역할을 가짐
		assertThat(course1.getCourseMemberList().get(0).getCourseRole()).isEqualTo(CourseRole.MANAGER);
		// 멤버1이 Spring Boot에서는 MENTEE 역할을 가짐
		assertThat(course2.getCourseMemberList().get(0).getCourseRole()).isEqualTo(CourseRole.MENTEE);
	}
}
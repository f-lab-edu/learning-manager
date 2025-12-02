package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseMemberTest {

	private Course course;
	private Long memberId;

	@BeforeEach
	void setUp() {
		course = Course.create("테스트 스터디", "스터디 설명");
		memberId = 1L;
	}

	@Test
	@DisplayName("[Success] MENTEE 역할로 과정 멤버를 성공적으로 생성한다.")
	void enroll_as_mentee_success() {
		CourseRole role = CourseRole.MENTEE;

		CourseMember courseMember = CourseMember.enroll(course, memberId, role);

		assertThat(courseMember).isNotNull();
		assertThat(courseMember.getCourse()).isEqualTo(course);
		assertThat(courseMember.getMemberId()).isEqualTo(memberId);
		assertThat(courseMember.getCourseRole()).isEqualTo(role);
	}

	@Test
	@DisplayName("[Success] MENTOR 역할로 과정 멤버를 성공적으로 생성한다.")
	void enroll_as_mentor_success() {
		CourseRole role = CourseRole.MENTOR;

		CourseMember courseMember = CourseMember.enroll(course, memberId, role);

		assertThat(courseMember).isNotNull();
		assertThat(courseMember.getCourse()).isEqualTo(course);
		assertThat(courseMember.getMemberId()).isEqualTo(memberId);
		assertThat(courseMember.getCourseRole()).isEqualTo(role);
	}

	@Test
	@DisplayName("[Success] MANAGER 역할로 과정 멤버를 성공적으로 생성한다.")
	void enroll_as_manager_success() {
		CourseRole role = CourseRole.MANAGER;

		CourseMember courseMember = CourseMember.enroll(course, memberId, role);

		assertThat(courseMember).isNotNull();
		assertThat(courseMember.getCourse()).isEqualTo(course);
		assertThat(courseMember.getMemberId()).isEqualTo(memberId);
		assertThat(courseMember.getCourseRole()).isEqualTo(role);
	}

	@Test
	@DisplayName("[Success] 멤버의 역할을 성공적으로 변경한다.")
	void change_role_success() {
		CourseMember courseMember = CourseMember.enroll(course, memberId, CourseRole.MENTEE);
		assertThat(courseMember.getCourseRole()).isEqualTo(CourseRole.MENTEE);

		courseMember.changeRole(CourseRole.MENTOR);

		assertThat(courseMember.getCourseRole()).isEqualTo(CourseRole.MENTOR);
	}
}

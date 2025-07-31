package me.chan99k.learningmanager.domain.course;

import static me.chan99k.learningmanager.domain.course.CourseProblemCode.*;
import static org.springframework.util.Assert.*;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity

public class CourseMember extends AbstractEntity {
	/**
	 * 사용자 ID
	 */
	private Long memberId;
	/**
	 * 과정 ID
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;
	/**
	 * 해당 과정에서의 역할
	 */
	@Enumerated(EnumType.STRING)
	private CourseRole courseRole;

	/* 도메인 로직 */

	public static CourseMember enroll(Course course, Long memberId, CourseRole courseRole) {
		notNull(course, COURSE_REQUIRED.getMessage());
		notNull(memberId, MEMBER_ID_REQUIRED.getMessage());
		notNull(courseRole, COURSE_ROLE_REQUIRED.getMessage());

		CourseMember courseMember = new CourseMember();
		courseMember.course = course;
		courseMember.memberId = memberId;
		courseMember.courseRole = courseRole;

		return courseMember;
	}

	public void changeRole(CourseRole newRole) {
		notNull(newRole, NEW_ROLE_REQUIRED.getMessage());
		this.courseRole = newRole;
	}

	/* 게터 로직 */

	public Long getMemberId() {
		return memberId;
	}

	public Course getCourse() {
		return course;
	}

	public CourseRole getCourseRole() {
		return courseRole;
	}
}

package me.chan99k.learningmanager.course;

import static me.chan99k.learningmanager.course.CourseProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Instant;

import me.chan99k.learningmanager.AbstractEntity;

public class CourseMember extends AbstractEntity {

	private Long memberId;

	private Course course;

	private CourseRole courseRole;

	protected CourseMember() {
	}

	public static CourseMember reconstitute(
		Long id,
		Long memberId,
		CourseRole courseRole,
		Instant createdAt,
		Long createdBy,
		Instant lastModifiedAt,
		Long lastModifiedBy,
		Long version
	) {
		CourseMember courseMember = new CourseMember();
		courseMember.setId(id);
		courseMember.memberId = memberId;
		courseMember.courseRole = courseRole;
		courseMember.setCreatedAt(createdAt);
		courseMember.setCreatedBy(createdBy);
		courseMember.setLastModifiedAt(lastModifiedAt);
		courseMember.setLastModifiedBy(lastModifiedBy);
		courseMember.setVersion(version);
		return courseMember;
	}

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

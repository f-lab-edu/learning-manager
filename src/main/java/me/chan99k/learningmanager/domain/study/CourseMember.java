package me.chan99k.learningmanager.domain.study;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
		CourseMember courseMember = new CourseMember();
		courseMember.course = course;
		courseMember.memberId = memberId;
		courseMember.courseRole = courseRole;

		return courseMember;
	}

	public void changeRole(CourseRole newRole) {
		this.courseRole = newRole;
	}

}

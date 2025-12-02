package me.chan99k.learningmanager.course.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.chan99k.learningmanager.common.MutableEntity;
import me.chan99k.learningmanager.course.CourseRole;

@Entity
@Table(name = "course_member")
public class CourseMemberEntity extends MutableEntity {

	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private CourseEntity course;

	@Enumerated(EnumType.STRING)
	private CourseRole courseRole;

	public CourseMemberEntity() {
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public CourseEntity getCourse() {
		return course;
	}

	public void setCourse(CourseEntity course) {
		this.course = course;
	}

	public CourseRole getCourseRole() {
		return courseRole;
	}

	public void setCourseRole(CourseRole courseRole) {
		this.courseRole = courseRole;
	}
}

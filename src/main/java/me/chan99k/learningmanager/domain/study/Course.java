package me.chan99k.learningmanager.domain.study;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AbstractEntity {
	/**
	 * 스터디 과정명
	 */
	private String title;
	/**
	 * 과정에 대한 간략한 설명
	 */
	private String description;
	/**
	 * 과정에 속한 멤버 목록
	 */
	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CourseMember> courseMemberList = new ArrayList<>();
	/**
	 * 과정에 속한 커리큘럼 목록
	 */
	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Curriculum> curriculumList = new ArrayList<>();

	/* 도메인 로직 */

	public static Course create(String title, String description) {
		Course course = new Course();
		course.title = title;
		course.description = description;
		return course;
	}

	public void update(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public void addMember(Long memberId, CourseRole courseRole) {
		CourseMember courseMember = CourseMember.enroll(this, memberId, courseRole);
		this.courseMemberList.add(courseMember);
	}
}

package me.chan99k.learningmanager.domain.course;

import static me.chan99k.learningmanager.domain.course.CourseProblemCode.*;
import static org.springframework.util.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
public class Course extends AbstractEntity {
	@Column(nullable = false, unique = true)
	private String title;

	private String description;

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CourseMember> courseMemberList = new ArrayList<>();

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Curriculum> curriculumList = new ArrayList<>();

	/* 도메인 로직 */

	public static Course create(String title, String description) {
		hasText(title, COURSE_TITLE_REQUIRED.getMessage());

		Course course = new Course();
		course.title = title;
		course.description = description;
		return course;
	}

	public void updateTitle(String newTitle) {
		hasText(newTitle, COURSE_TITLE_REQUIRED.getMessage());
		this.title = newTitle;
	}

	public void updateDescription(String newDescription) {
		hasText(newDescription, COURSE_DESCRIPTION_REQUIRED.getMessage());
		this.description = newDescription;
	}

	public void addMember(Long memberId, CourseRole courseRole) {
		boolean alreadyExists = this.courseMemberList.stream().anyMatch(
			member -> member.getMemberId().equals(memberId)
		);

		isTrue(!alreadyExists, COURSE_MEMBER_ALREADY_REGISTERED.getMessage());

		CourseMember courseMember = CourseMember.enroll(this, memberId, courseRole);
		this.courseMemberList.add(courseMember);
	}

	public void removeMember(Long memberId) {
		boolean removed = this.courseMemberList.removeIf(
			courseMember -> courseMember.getMemberId().equals(memberId)
		);

		isTrue(removed, COURSE_MEMBER_NOT_REGISTERED.getMessage());
	}

	public Curriculum addCurriculum(String title, String description) {
		Curriculum curriculum = Curriculum.create(this, title, description);

		this.curriculumList.add(curriculum);
		return curriculum;
	}

	public void removeCurriculum(Curriculum curriculum) {
		notNull(curriculum, CURRICULUM_NULL.getMessage());

		boolean removed = this.curriculumList.remove(curriculum);

		isTrue(removed, CURRICULUM_NOT_FOUND_IN_COURSE.getMessage() + " ID: " + curriculum.getId());
	}

	public Curriculum findCurriculumById(Long curriculumId) {
		notNull(curriculumId, "Curriculum ID is required");

		return this.curriculumList.stream()
			.filter(curriculum -> curriculum.getId().equals(curriculumId))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(
				CURRICULUM_NOT_FOUND_IN_COURSE.getMessage() + " ID: " + curriculumId
			));
	}

	/* 게터 로직 */

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public List<CourseMember> getCourseMemberList() {
		return Collections.unmodifiableList(courseMemberList);
	}

	public List<Curriculum> getCurriculumList() {
		return Collections.unmodifiableList(curriculumList);
	}
}

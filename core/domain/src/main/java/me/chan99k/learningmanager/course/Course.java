package me.chan99k.learningmanager.course;

import static me.chan99k.learningmanager.course.CourseProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import me.chan99k.learningmanager.AbstractEntity;

public class Course extends AbstractEntity {

	private String title;

	private String description;

	private List<CourseMember> courseMemberList = new ArrayList<>();

	private List<Curriculum> curriculumList = new ArrayList<>();

	protected Course() {
	}

	public static Course reconstitute(
		Long id,
		String title,
		String description,
		List<CourseMember> courseMemberList,
		List<Curriculum> curriculumList,
		Instant createdAt,
		Long createdBy,
		Instant lastModifiedAt,
		Long lastModifiedBy,
		Long version
	) {
		Course course = new Course();
		course.setId(id);
		course.title = title;
		course.description = description;
		course.courseMemberList = new ArrayList<>(courseMemberList);
		course.curriculumList = new ArrayList<>(curriculumList);
		course.setCreatedAt(createdAt);
		course.setCreatedBy(createdBy);
		course.setLastModifiedAt(lastModifiedAt);
		course.setLastModifiedBy(lastModifiedBy);
		course.setVersion(version);
		return course;
	}

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
		notNull(curriculumId, "[System] 커리큘럼 ID 는 필수입니다");

		return this.curriculumList.stream()
			.filter(curriculum -> Objects.equals(curriculum.getId(), curriculumId))
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

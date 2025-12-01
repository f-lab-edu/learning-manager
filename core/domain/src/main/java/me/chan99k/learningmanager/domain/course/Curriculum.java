package me.chan99k.learningmanager.domain.course;

import static me.chan99k.learningmanager.domain.course.CourseProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Instant;

import me.chan99k.learningmanager.domain.AbstractEntity;

public class Curriculum extends AbstractEntity {

	private Course course;

	private String title;

	private String description;

	protected Curriculum() {
	}

	public static Curriculum reconstitute(
		Long id,
		String title,
		String description,
		Instant createdAt,
		Long createdBy,
		Instant lastModifiedAt,
		Long lastModifiedBy,
		Long version
	) {
		Curriculum curriculum = new Curriculum();
		curriculum.setId(id);
		curriculum.title = title;
		curriculum.description = description;
		curriculum.setCreatedAt(createdAt);
		curriculum.setCreatedBy(createdBy);
		curriculum.setLastModifiedAt(lastModifiedAt);
		curriculum.setLastModifiedBy(lastModifiedBy);
		curriculum.setVersion(version);
		return curriculum;
	}

	/* 도메인 로직 */

	public static Curriculum create(Course course, String title, String description) {
		notNull(course, CURRICULUM_COURSE_REQUIRED.getMessage());
		hasText(title, CURRICULUM_TITLE_REQUIRED.getMessage());

		Curriculum curriculum = new Curriculum();
		curriculum.course = course;
		curriculum.title = title;
		curriculum.description = description;
		return curriculum;
	}

	public void updateTitle(String newTitle) {
		hasText(newTitle, CURRICULUM_TITLE_REQUIRED.getMessage());
		this.title = newTitle;
	}

	public void updateDescription(String newDescription) {
		hasText(newDescription, CURRICULUM_DESCRIPTION_REQUIRED.getMessage());
		this.description = newDescription;
	}

	/* 게터 로직 */

	public Course getCourse() {
		return course;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
}

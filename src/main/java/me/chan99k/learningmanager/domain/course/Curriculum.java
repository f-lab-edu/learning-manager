package me.chan99k.learningmanager.domain.course;

import static me.chan99k.learningmanager.domain.course.CourseProblemCode.*;
import static org.springframework.util.Assert.*;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
public class Curriculum extends AbstractEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	private String title;

	private String description;

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

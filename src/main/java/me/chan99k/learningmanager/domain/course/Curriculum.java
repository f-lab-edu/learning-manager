package me.chan99k.learningmanager.domain.course;

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
		notNull(course, "[System] 커리큘럼은 반드시 코스에 속해야 합니다.");
		hasText(title, "[System] 커리큘럼명은 필수입니다.");

		Curriculum curriculum = new Curriculum();
		curriculum.course = course;
		curriculum.title = title;
		curriculum.description = description;
		return curriculum;
	}

	public void updateTitle(String newTitle) {
		hasText(newTitle, "[System] 커리큘럼명 값이 비어 있습니다.");
		this.title = newTitle;
	}

	public void updateDescription(String newDescription) {
		hasText(newDescription, "[System] 커리큘럼에 대한 설명 값이 비어 있습니다.");
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

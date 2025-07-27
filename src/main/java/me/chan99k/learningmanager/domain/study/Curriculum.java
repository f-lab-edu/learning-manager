package me.chan99k.learningmanager.domain.study;

import static org.springframework.util.Assert.*;

import org.springframework.util.StringUtils;

import jakarta.persistence.Entity;
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

	public void update(String title, String description) {
		if (StringUtils.hasText(title)) {
			this.title = title;
		}
		if (description != null) {
			this.description = description;
		}
	}
}

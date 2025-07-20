package me.chan99k.learningmanager.domain.study;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum extends AbstractEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;
	/**
	 * 커리큘럼명
	 */
	private String title;
	/**
	 * 커리큘럼에 대한 간략한 설명
	 */
	private String description;
	/**
	 * 커리큘럼에 속한 세션 목록
	 */
	@OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Session> sessionList = new ArrayList<>(); // TODO : 세션의 생명주기가 커리큘럼에 완전히 종속적인지 생각해보기

	/* 도메인 로직 */

	public static Curriculum create(Course course, String title, String description) {
		Curriculum curriculum = new Curriculum();
		curriculum.course = course;
		curriculum.title = title;
		curriculum.description = description;
		return curriculum;
	}

	public void update(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public void addSession(Session session) {
		this.sessionList.add(session);
	}

	public void detachSessionFromCurriculum(Session session) {
		this.sessionList.remove(session);
	}
}

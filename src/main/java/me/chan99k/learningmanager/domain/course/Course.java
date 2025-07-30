package me.chan99k.learningmanager.domain.course;

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
		hasText(title, "[시스템] 과정명은 필수값 입니다.");

		Course course = new Course();
		course.title = title;
		course.description = description;
		return course;
	}

	public void updateTitle(String newTitle) {
		hasText(newTitle, "[System] 과정명 값이 비어 있습니다.");
		this.title = newTitle;
	}

	public void updateDescription(String newDescription) {
		hasText(newDescription, "[System] 과정에 대한 설명 값이 비어 있습니다.");
		this.description = newDescription;
	}

	public void addMember(Long memberId, CourseRole courseRole) {
		boolean alreadyExists = this.courseMemberList.stream().anyMatch(
			member -> member.getMemberId().equals(memberId)
		);

		isTrue(!alreadyExists, "[System] 이미 과정에 등록된 멤버입니다.");

		CourseMember courseMember = CourseMember.enroll(this, memberId, courseRole);
		this.courseMemberList.add(courseMember);
	}

	public void removeMember(Long memberId) {
		boolean removed = this.courseMemberList.removeIf(
			courseMember -> courseMember.getMemberId().equals(memberId)
		);

		isTrue(removed, "[System] 과정에 등록되지 않은 멤버입니다.");
	}

	public void addCurriculum(String title, String description) {
		Curriculum curriculum = Curriculum.create(this, title, description);

		this.curriculumList.add(curriculum);
	}

	public void removeCurriculum(Curriculum curriculum) {
		notNull(curriculum, "[System] 제거할 커리큘럼은 null일 수 없습니다.");

		boolean removed = this.curriculumList.remove(curriculum);

		isTrue(removed, "[System] 해당 과정에 존재하지 않는 커리큘럼입니다. ID: " + curriculum.getId());
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

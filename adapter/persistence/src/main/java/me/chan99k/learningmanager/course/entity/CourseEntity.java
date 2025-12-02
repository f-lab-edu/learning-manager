package me.chan99k.learningmanager.course.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import me.chan99k.learningmanager.common.MutableEntity;

@Entity
@Table(name = "course")
public class CourseEntity extends MutableEntity {

	@Column(nullable = false, unique = true)
	private String title;

	private String description;

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CourseMemberEntity> courseMemberList = new ArrayList<>();

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CurriculumEntity> curriculumList = new ArrayList<>();

	public CourseEntity() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<CourseMemberEntity> getCourseMemberList() {
		return courseMemberList;
	}

	public void setCourseMemberList(List<CourseMemberEntity> courseMemberList) {
		this.courseMemberList = courseMemberList;
	}

	public void addCourseMember(CourseMemberEntity courseMember) {
		courseMemberList.add(courseMember);
		courseMember.setCourse(this);
	}

	public List<CurriculumEntity> getCurriculumList() {
		return curriculumList;
	}

	public void setCurriculumList(List<CurriculumEntity> curriculumList) {
		this.curriculumList = curriculumList;
	}

	public void addCurriculum(CurriculumEntity curriculum) {
		curriculumList.add(curriculum);
		curriculum.setCourse(this);
	}
}

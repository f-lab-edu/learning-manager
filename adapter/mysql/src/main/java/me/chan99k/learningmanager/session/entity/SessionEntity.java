package me.chan99k.learningmanager.session.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import me.chan99k.learningmanager.common.MutableEntity;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

@Entity
@Table(name = "session")
public class SessionEntity extends MutableEntity {

	@Column(name = "course_id")
	private Long courseId;

	@Column(name = "curriculum_id")
	private Long curriculumId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private SessionEntity parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SessionEntity> children = new ArrayList<>();

	@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SessionParticipantEntity> participants = new ArrayList<>();

	@Column(nullable = false)
	private String title;

	private Instant scheduledAt;

	private Instant scheduledEndAt;

	@Enumerated(EnumType.STRING)
	private SessionType type;

	@Enumerated(EnumType.STRING)
	private SessionLocation location;

	private String locationDetails;

	public SessionEntity() {
	}

	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}

	public Long getCurriculumId() {
		return curriculumId;
	}

	public void setCurriculumId(Long curriculumId) {
		this.curriculumId = curriculumId;
	}

	public SessionEntity getParent() {
		return parent;
	}

	public void setParent(SessionEntity parent) {
		this.parent = parent;
	}

	public List<SessionEntity> getChildren() {
		return children;
	}

	public void setChildren(List<SessionEntity> children) {
		this.children = children;
	}

	public void addChild(SessionEntity child) {
		children.add(child);
		child.setParent(this);
	}

	public List<SessionParticipantEntity> getParticipants() {
		return participants;
	}

	public void setParticipants(List<SessionParticipantEntity> participants) {
		this.participants = participants;
	}

	public void addParticipant(SessionParticipantEntity participant) {
		participants.add(participant);
		participant.setSession(this);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Instant getScheduledAt() {
		return scheduledAt;
	}

	public void setScheduledAt(Instant scheduledAt) {
		this.scheduledAt = scheduledAt;
	}

	public Instant getScheduledEndAt() {
		return scheduledEndAt;
	}

	public void setScheduledEndAt(Instant scheduledEndAt) {
		this.scheduledEndAt = scheduledEndAt;
	}

	public SessionType getType() {
		return type;
	}

	public void setType(SessionType type) {
		this.type = type;
	}

	public SessionLocation getLocation() {
		return location;
	}

	public void setLocation(SessionLocation location) {
		this.location = location;
	}

	public String getLocationDetails() {
		return locationDetails;
	}

	public void setLocationDetails(String locationDetails) {
		this.locationDetails = locationDetails;
	}
}

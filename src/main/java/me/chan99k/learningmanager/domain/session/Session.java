package me.chan99k.learningmanager.domain.session;

import static me.chan99k.learningmanager.domain.session.SessionProblemCode.*;
import static org.springframework.util.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.util.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
public class Session extends AbstractEntity {
	@Column(name = "course_id")
	private Long courseId;

	@Column(name = "curriculum_id")
	private Long curriculumId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Session parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Session> children = new ArrayList<>();

	@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SessionParticipant> participants = new ArrayList<>();

	@Column(nullable = false)
	private String title;

	private Instant scheduledAt;

	private Instant scheduledEndAt;

	@Enumerated(EnumType.STRING)
	private SessionType type;

	@Enumerated(EnumType.STRING)
	private SessionLocation location;

	private String locationDetails;

	protected Session() {
	}

	private Session(String title, Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		this.title = title;
		this.scheduledAt = scheduledAt;
		this.scheduledEndAt = scheduledEndAt;
		this.type = type;
		this.location = location;
		this.locationDetails = locationDetails;
	}

	// TODO :: 담당 매니저,멘토가 같으면서 루트/하위 세션 타입도 같은 세션은 서로 시간이 겹치면 안된다는 제약 조건을 추가하여야 함
	/* Domain Logic */

	public static Session createStandaloneSession(String title, Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		Session session = new Session(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
		session.validate();
		return session;
	}

	public static Session createCourseSession(Long courseId, String title,
		Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		notNull(courseId, COURSE_ID_REQUIRED.getMessage());
		Session session = new Session(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
		session.courseId = courseId;
		session.validate();
		return session;
	}

	public static Session createCurriculumSession(Long courseId, Long curriculumId, String title, Instant scheduledAt,
		Instant scheduledEndAt, SessionType type, SessionLocation location, String locationDetails) {
		notNull(courseId, COURSE_ID_REQUIRED.getMessage());
		notNull(curriculumId, CURRICULUM_ID_REQUIRED.getMessage());
		Session session = new Session(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
		session.courseId = courseId;
		session.curriculumId = curriculumId;
		session.validate();
		return session;
	}

	public Session createChildSession(String title, Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		isTrue(this.isRootSession(), INVALID_SESSION_HIERARCHY.getMessage());

		Session child = new Session(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
		child.parent = this;
		// 하위 세션은 부모의 소속(코스, 커리큘럼)을 상속
		child.courseId = this.courseId;
		child.curriculumId = this.curriculumId;

		child.validate(); // 하위 세션 자체의 유효성 및 부모와의 관계 유효성 검증
		this.children.add(child);
		return child;
	}

	public void addParticipant(Long memberId, SessionParticipantRole role) {
		boolean alreadyExists = this.participants.stream()
			.anyMatch(p -> p.getMemberId().equals(memberId));
		isTrue(!alreadyExists, ALREADY_PARTICIPATING_MEMBER.getMessage());

		SessionParticipant participant = SessionParticipant.of(memberId, this, role);
		this.participants.add(participant);
	}

	public void removeParticipant(Long memberId) {
		boolean removed = this.participants.removeIf(p -> p.getMemberId().equals(memberId));
		isTrue(removed, MEMBER_NOT_PARTICIPATING.getMessage());
	}

	public void changeParticipantRole(Long memberId, SessionParticipantRole newRole) {
		// 수정 가능 여부를 먼저 검증
		validateUpdatable();

		// 대상 참여자를 찾아서 실제 역할 변경을 위임
		SessionParticipant participant = findParticipant(memberId);
		participant.changeRole(newRole);
	}

	private SessionParticipant findParticipant(Long memberId) {
		return this.participants.stream()
			.filter(p -> p.getMemberId().equals(memberId))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(MEMBER_NOT_PARTICIPATING.getMessage()));
	}

	/**
	 * 세션의 시간을 재조정합니다.
	 */
	public void reschedule(Instant newScheduledAt, Instant newScheduledEndAt) {
		validateUpdatable();
		this.scheduledAt = newScheduledAt;
		this.scheduledEndAt = newScheduledEndAt;
		validate();
	}

	/**
	 * 세션의 기본 정보를 변경합니다.
	 */
	public void changeInfo(String newTitle, SessionType newType) {
		validateUpdatable();
		this.title = newTitle;
		this.type = newType;
		validate();
	}

	/**
	 * 세션의 장소를 변경합니다.
	 */
	public void changeLocation(SessionLocation newLocation, String newLocationDetails) {
		validateUpdatable();
		this.location = newLocation;
		this.locationDetails = newLocationDetails;
		validate();
	}

	private void validate() {
		validateSessionTime();
		validateLocation();
		validateHierarchy();
	}

	private void validateUpdatable() {
		Instant now = Instant.now();
		isTrue(now.isBefore(this.scheduledAt), CANNOT_MODIFY_STARTED_SESSION.getMessage());

		if (isRootSession()) {
			// 루트 세션은 시작 3일 전까지만 수정 가능
			isTrue(now.isBefore(this.scheduledAt.minus(3, ChronoUnit.DAYS)),
				ROOT_SESSION_MODIFICATION_DEADLINE_EXCEEDED.getMessage());
		} else {
			// 하위 세션은 시작 1시간 전까지만 수정 가능
			isTrue(now.isBefore(this.scheduledAt.minus(1, ChronoUnit.HOURS)),
				CHILD_SESSION_MODIFICATION_DEADLINE_EXCEEDED.getMessage());
		}
	}

	private void validateSessionTime() {
		notNull(scheduledAt, SESSION_START_TIME_REQUIRED.getMessage());
		notNull(scheduledEndAt, SESSION_END_TIME_REQUIRED.getMessage());
		isTrue(scheduledAt.isBefore(scheduledEndAt), START_TIME_MUST_BE_BEFORE_END_TIME.getMessage());

		long durationHours = Duration.between(scheduledAt, scheduledEndAt).toHours();
		isTrue(durationHours < 24, SESSION_DURATION_EXCEEDS_24_HOURS.getMessage());

		long startDay = scheduledAt.truncatedTo(ChronoUnit.DAYS).toEpochMilli();
		long endDay = scheduledEndAt.truncatedTo(ChronoUnit.DAYS).toEpochMilli();

		isTrue(startDay == endDay, SESSION_CANNOT_SPAN_MULTIPLE_DAYS.getMessage());
	}

	private void validateLocation() {
		if (location == SessionLocation.SITE) {
			hasText(locationDetails, OFFLINE_SESSION_LOCATION_DETAIL_REQUIRED.getMessage());
		} else {
			isTrue(!StringUtils.hasText(locationDetails), ONLINE_SESSION_CANNOT_HAVE_LOCATION_DETAIL.getMessage());
		}
	}

	private void validateHierarchy() {
		if (isChildSession()) {
			notNull(parent, CHILD_SESSION_MUST_HAVE_PARENT.getMessage());
			isTrue(!scheduledAt.isBefore(parent.getScheduledAt()), CHILD_SESSION_START_TIME_BEFORE_PARENT.getMessage());
			isTrue(!scheduledEndAt.isAfter(parent.getScheduledEndAt()),
				CHILD_SESSION_END_TIME_AFTER_PARENT.getMessage());
		}
	}

	public Instant getScheduledAt() {
		return scheduledAt;
	}

	public Instant getScheduledEndAt() {
		return scheduledEndAt;
	}

	public List<SessionParticipant> getParticipants() {
		return Collections.unmodifiableList(this.participants);
	}

	public boolean isRootSession() {
		return Objects.isNull(this.parent);
	}

	public boolean isChildSession() {
		return !this.isRootSession();
	}

	public Long getCourseId() {
		return courseId;
	}

	public Long getCurriculumId() {
		return curriculumId;
	}

	public Session getParent() {
		return parent;
	}

	public List<Session> getChildren() {
		return children;
	}

	public String getTitle() {
		return title;
	}

	public SessionType getType() {
		return type;
	}

	public SessionLocation getLocation() {
		return location;
	}

	public String getLocationDetails() {
		return locationDetails;
	}
}

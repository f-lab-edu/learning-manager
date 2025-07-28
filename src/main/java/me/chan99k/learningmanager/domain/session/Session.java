package me.chan99k.learningmanager.domain.session;

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
import lombok.Getter;
import me.chan99k.learningmanager.domain.AbstractEntity;

@Entity
@Getter
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

	private String title;

	private Instant scheduledAt;

	private Instant scheduledEndAt;

	@Enumerated(EnumType.STRING)
	private SessionType type;

	@Enumerated(EnumType.STRING)
	private SessionLocation location;

	private String locationDetails;

	// TODO :: 담당 매니저,멘토가 같으면서 루트/하위 세션 타입도 같은 세션은 서로 시간이 겹치면 안된다는 제약 조건을 추가하여야 함
	/* Domain Logic */

	private static Session create(String title, Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		Session session = new Session();
		session.title = title;
		session.scheduledAt = scheduledAt;
		session.scheduledEndAt = scheduledEndAt;
		session.type = type;
		session.location = location;
		session.locationDetails = locationDetails;
		return session;
	}

	public static Session createStandaloneSession(String title, Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		Session session = create(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
		session.validate();
		return session;
	}

	public static Session createCourseSession(Long courseId, String title,
		Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		notNull(courseId, "[System] 코스 ID는 필수입니다.");
		Session session = create(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
		session.courseId = courseId;
		session.validate();
		return session;
	}

	public static Session createCurriculumSession(Long courseId, Long curriculumId, String title, Instant scheduledAt,
		Instant scheduledEndAt, SessionType type, SessionLocation location, String locationDetails) {
		notNull(courseId, "[System] 코스 ID는 필수입니다.");
		notNull(curriculumId, "[System] 커리큘럼 ID는 필수입니다.");
		Session session = create(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
		session.courseId = courseId;
		session.curriculumId = curriculumId;
		session.validate();
		return session;
	}

	public Session createChildSession(String title, Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		isTrue(this.isRootSession(), "[System] 하위 세션은 또 다른 하위 세션을 가질 수 없습니다.");

		Session child = create(title, scheduledAt, scheduledEndAt, type, location, locationDetails);
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
		isTrue(!alreadyExists, "[System] 이미 세션에 참여 중인 멤버입니다.");

		SessionParticipant participant = SessionParticipant.of(this, memberId, role);
		this.participants.add(participant);
	}

	public void removeParticipant(Long memberId) {
		this.participants.removeIf(p -> p.getMemberId().equals(memberId));
	}

	public void update(String title, Instant scheduledAt,
		Instant scheduledEndAt, SessionType type,
		SessionLocation location, String locationDetails
	) {

		validateUpdatable();

		this.title = title;
		this.scheduledAt = scheduledAt;
		this.scheduledEndAt = scheduledEndAt;
		this.type = type;
		this.location = location;
		this.locationDetails = locationDetails;

		validate();
	}

	private void validate() {
		validateSessionTime();
		validateLocation();
		validateHierarchy();
	}

	private void validateUpdatable() {
		Instant now = Instant.now();
		isTrue(now.isBefore(this.scheduledAt), "[System] 이미 시작된 세션은 수정할 수 없습니다.");

		if (isRootSession()) {
			// 루트 세션은 시작 3일 전까지만 수정 가능
			isTrue(now.isBefore(this.scheduledAt.minus(3, ChronoUnit.DAYS)),
				"[System] 루트 세션은 시작 3일 전까지만 수정할 수 있습니다.");
		} else {
			// 하위 세션은 시작 1시간 전까지만 수정 가능
			isTrue(now.isBefore(this.scheduledAt.minus(1, ChronoUnit.HOURS)),
				"[System] 하위 세션은 시작 1시간 전까지만 수정할 수 있습니다.");
		}
	}

	private void validateSessionTime() {
		notNull(scheduledAt, "[System] 세션 시작 시간은 필수입니다.");
		notNull(scheduledEndAt, "[System] 세션 종료 시간은 필수입니다.");
		isTrue(scheduledAt.isBefore(scheduledEndAt), "[System] 세션 시작 시간은 종료 시간보다 빨라야 합니다.");

		long durationHours = Duration.between(scheduledAt, scheduledEndAt).toHours();
		isTrue(durationHours < 24, "[System] 세션은 24시간을 초과할 수 없습니다.");

		long startDay = scheduledAt.truncatedTo(ChronoUnit.DAYS).toEpochMilli();
		long endDay = scheduledEndAt.truncatedTo(ChronoUnit.DAYS).toEpochMilli();

		isTrue(startDay == endDay, "[System] 세션은 이틀에 걸쳐 진행될 수 없습니다.");
	}

	private void validateLocation() {
		if (location == SessionLocation.SITE) {
			hasText(locationDetails, "[System] 오프라인 세션은 상세 장소 설명이 필수입니다.");
		} else {
			isTrue(!StringUtils.hasText(locationDetails), "[System] 온라인 세션은 상세 장소 설명을 가질 수 없습니다.");
		}
	}

	private void validateHierarchy() {
		if (isChildSession()) {
			notNull(parent, "[System] 하위 세션은 반드시 부모 세션을 가져야 합니다.");
			isTrue(!scheduledAt.isBefore(parent.getScheduledAt()), "[System] 하위 세션의 시작 시간은 부모 세션의 시작 시간보다 빠를 수 없습니다.");
			isTrue(!scheduledEndAt.isAfter(parent.getScheduledEndAt()),
				"[System] 하위 세션의 종료 시간은 부모 세션의 종료 시간보다 늦을 수 없습니다.");
		}
	}

	public List<SessionParticipant> getParticipants() {
		return Collections.unmodifiableList(this.participants);
	}

	boolean isRootSession() {
		return Objects.isNull(this.parent);
	}

	boolean isChildSession() {
		return !this.isRootSession();
	}
}

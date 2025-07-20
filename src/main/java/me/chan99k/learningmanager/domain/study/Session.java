package me.chan99k.learningmanager.domain.study;

import static org.springframework.util.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Session extends AbstractEntity {
	/**
	 * 커리큘럼 ID
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "curriculum_id")
	private Curriculum curriculum;
	/**
	 * 상위 세션 ID
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Session parent;
	/**
	 * 하위 세션 목록
	 */
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Session> children = new ArrayList<>();
	/**
	 * 세션명
	 */
	private String title;
	/**
	 * 세션 시작 시간
	 */
	private Instant scheduledAt;
	/**
	 * 세션 종료 시간
	 */
	private Instant scheduledEndAt;
	/**
	 * 세션 타입
	 */
	@Enumerated(EnumType.STRING)
	private SessionType type;
	/**
	 * 세션 장소
	 */
	@Enumerated(EnumType.STRING)
	private SessionLocation location;
	/**
	 * sessionLocation이 SITE일 경우에만 사용될 상세 장소 설명
	 */
	private String locationDetails;

	/* 도메인 로직 */

	public static Session createRootSession(Curriculum curriculum, String title, Instant scheduledAt, Instant scheduledEndAt,
		SessionType type, SessionLocation location, String locationDetails
	) {
		Session session = new Session();

		session.curriculum = curriculum;
		session.title = title;
		session.scheduledAt = scheduledAt;
		session.scheduledEndAt = scheduledEndAt;
		session.type = type;
		session.location = location;
		session.locationDetails = locationDetails;
		session.parent = null; // 최상위 세션, parent는 null

		session.validate();

		return session;
	}

	public static Session createSubSession(Session parentSession, String title, Instant scheduledAt,
		Instant scheduledEndAt, SessionType type, SessionLocation location, String locationDetails
	) {
		isTrue(parentSession.isRootSession(), "[System] 하위 세션은 또 다른 하위 세션을 가질 수 없습니다.");

		Session subSession = new Session();
		subSession.curriculum = parentSession.getCurriculum();
		subSession.title = title;
		subSession.scheduledAt = scheduledAt;
		subSession.scheduledEndAt = scheduledEndAt;
		subSession.type = type;
		subSession.location = location;
		subSession.locationDetails = locationDetails;
		subSession.parent = parentSession;

		subSession.validate();

		parentSession.getChildren().add(subSession);

		return subSession;
	}

	public void update(String title, Instant scheduledAt, Instant scheduledEndAt, SessionType type,
		SessionLocation location, String locationDetails) {
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

	private void validateSessionTime() {
		notNull(scheduledAt, "[System] 세션 시작 시간은 필수입니다.");
		notNull(scheduledEndAt, "[System] 세션 종료 시간은 필수입니다.");
		isTrue(scheduledAt.isBefore(scheduledEndAt), "[System] 세션 시작 시간은 종료 시간보다 빨라야 합니다.");

		long durationHours = Duration.between(scheduledAt, scheduledEndAt).toHours();
		isTrue(durationHours < 24, "[System] 세션은 24시간을 초과할 수 없습니다.");

		long startDay = scheduledAt.truncatedTo(ChronoUnit.DAYS).toEpochMilli();
		long endDay = scheduledEndAt.truncatedTo(ChronoUnit.DAYS).toEpochMilli();

		isTrue(startDay == endDay, "[System] 세션은 이틀에 걸쳐 분포할 수 없습니다.");
	}

	private void validateLocation() {
		if (location == SessionLocation.SITE) {
			hasText(locationDetails, "[System] 오프라인 세션은 상세 장소 설명이 필수입니다.");
		} else {
			// SITE가 아닐 경우 locationDetails는 null 이거나 비어 있어야 함
			isTrue(!StringUtils.hasText(locationDetails), "[System] 온라인 세션은 상세 장소 설명을 가질 수 없습니다.");
		}
	}

	private void validateHierarchy() {
		if (isChildSession()) {
			notNull(parent, "[System] 하위 세션은 반드시 부모 세션을 가져야 합니다.");
			// 하위 세션의 시간은 부모 세션의 시간 범위 내에 있어야 함
			isTrue(!scheduledAt.isBefore(parent.getScheduledAt()), "[System] 하위 세션의 시작 시간은 부모 세션의 시작 시간보다 빠를 수 없습니다.");
			isTrue(!scheduledEndAt.isAfter(parent.getScheduledEndAt()),
				"[System] 하위 세션의 종료 시간은 부모 세션의 종료 시간보다 늦을 수 없습니다.");
		}
	}

	private boolean isRootSession() {
		return Objects.isNull(parent);
	}

	private boolean isChildSession() {
		return !isRootSession();
	}
}

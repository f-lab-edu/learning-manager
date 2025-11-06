package me.chan99k.learningmanager.adapter.persistence.session;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import me.chan99k.learningmanager.config.TestJpaConfig;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.Curriculum;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

@DataJpaTest
@Testcontainers
@Import(TestJpaConfig.class)
@DisplayName("SessionJpaRepository 테스트")
class SessionJpaRepositoryTest {

	private static final Instant START_DATE = Instant.parse("2025-01-01T00:00:00Z");
	private static final Instant END_DATE = Instant.parse("2025-01-31T23:59:59Z");
	private final Clock clock = Clock.systemUTC();
	@Autowired
	private TestEntityManager entityManager;
	@Autowired
	private SessionJpaRepository sessionJpaRepository;
	private Course course1;
	private Course course2;
	private Curriculum curriculum1;
	private Curriculum curriculum2;
	private Session session1;
	private Session session2;
	private Session session3;
	private Session session4;
	private Session session5;

	@BeforeEach
	void setUp() {
		// Course 및 Curriculum 엔티티 생성
		course1 = Course.create("Java Programming Course", "Learn Java fundamentals");
		course2 = Course.create("Spring Boot Course", "Learn Spring Boot framework");
		entityManager.persistAndFlush(course1);
		entityManager.persistAndFlush(course2);

		curriculum1 = Curriculum.create(course1, "Java Basics", "Introduction to Java");
		curriculum2 = Curriculum.create(course1, "Advanced Java", "Advanced Java concepts");
		entityManager.persistAndFlush(curriculum1);
		entityManager.persistAndFlush(curriculum2);

		// Session 엔티티 생성 (실제 Course, Curriculum ID 사용)
		session1 = Session.createCurriculumSession(
			course1.getId(), curriculum1.getId(), "Session 1",
			Instant.parse("2025-01-10T10:00:00Z"),
			Instant.parse("2025-01-10T12:00:00Z"),
			SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null, clock
		);

		session2 = Session.createCurriculumSession(
			course1.getId(), curriculum2.getId(), "Session 2",
			Instant.parse("2025-01-20T14:00:00Z"),
			Instant.parse("2025-01-20T16:00:00Z"),
			SessionType.OFFLINE, SessionLocation.SITE, "Room A", clock
		);

		session3 = Session.createCurriculumSession(
			course2.getId(), curriculum1.getId(), "Session 3",
			Instant.parse("2025-01-25T16:00:00Z"),
			Instant.parse("2025-01-25T18:00:00Z"),
			SessionType.ONLINE, SessionLocation.ZOOM, null, clock
		);

		session4 = Session.createCurriculumSession(
			course1.getId(), curriculum1.getId(), "Session 4",
			Instant.parse("2024-12-25T10:00:00Z"),
			Instant.parse("2024-12-25T12:00:00Z"),
			SessionType.OFFLINE, SessionLocation.SITE, "Room B", clock
		);

		session5 = Session.createCurriculumSession(
			course2.getId(), curriculum2.getId(), "Session 5",
			Instant.parse("2025-02-10T10:00:00Z"),
			Instant.parse("2025-02-10T12:00:00Z"),
			SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null, clock
		);

		entityManager.persistAndFlush(session1);
		entityManager.persistAndFlush(session2);
		entityManager.persistAndFlush(session3);
		entityManager.persistAndFlush(session4);
		entityManager.persistAndFlush(session5);
	}

	@Test
	@DisplayName("SessionInfo 조인 조회 - 기존 세션들로 테스트")
	void findSessionInfoProjectionByIds_WithExistingSessions_Success() {
		List<Long> sessionIds = List.of(session1.getId(), session2.getId());

		List<SessionInfo> result = sessionJpaRepository.findSessionInfoProjectionByIds(sessionIds);

		assertThat(result).hasSize(2);

		SessionInfo projection1 = result.stream()
			.filter(p -> p.sessionId().equals(session1.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(projection1.sessionTitle()).isEqualTo("Session 1");
		assertThat(projection1.courseId()).isEqualTo(course1.getId());
		assertThat(projection1.courseTitle()).isEqualTo("Java Programming Course");
		assertThat(projection1.curriculumId()).isEqualTo(curriculum1.getId());
		assertThat(projection1.curriculumTitle()).isEqualTo("Java Basics");

		SessionInfo projection2 = result.stream()
			.filter(p -> p.sessionId().equals(session2.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(projection2.sessionTitle()).isEqualTo("Session 2");
		assertThat(projection2.courseId()).isEqualTo(course1.getId());
		assertThat(projection2.courseTitle()).isEqualTo("Java Programming Course");
		assertThat(projection2.curriculumId()).isEqualTo(curriculum2.getId());
		assertThat(projection2.curriculumTitle()).isEqualTo("Advanced Java");
	}

	@Test
	@DisplayName("SessionInfo 조회 - 존재하지 않는 세션 ID들")
	void findSessionInfoProjectionByIds_NonExistentSessionIds_EmptyResult() {
		List<SessionInfo> result = sessionJpaRepository.findSessionInfoProjectionByIds(
			List.of(999L, 1000L)
		);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - 모든 필터 null")
	void findIdsByPeriodAndFilters_AllFiltersNull_Success() {
		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			START_DATE, END_DATE, null, null
		);

		assertThat(result).hasSize(3);
		assertThat(result).containsExactlyInAnyOrder(
			session1.getId(), session2.getId(), session3.getId()
		);
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - courseId 필터링")
	void findIdsByPeriodAndFilters_WithCourseIdFilter_Success() {
		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			START_DATE, END_DATE, course1.getId(), null
		);

		assertThat(result).hasSize(2);
		assertThat(result).containsExactlyInAnyOrder(
			session1.getId(), session2.getId()
		);
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - curriculumId 필터링")
	void findIdsByPeriodAndFilters_WithCurriculumIdFilter_Success() {
		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			START_DATE, END_DATE, null, curriculum1.getId()
		);

		assertThat(result).hasSize(2);
		assertThat(result).containsExactlyInAnyOrder(
			session1.getId(), session3.getId()
		);
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - courseId와 curriculumId 모두 필터링")
	void findIdsByPeriodAndFilters_WithBothFilters_Success() {
		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			START_DATE, END_DATE, course1.getId(), curriculum1.getId()
		);

		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(session1.getId());
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - 존재하지 않는 courseId로 필터링")
	void findIdsByPeriodAndFilters_NonExistentCourseId_EmptyResult() {
		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			START_DATE, END_DATE, 999L, null
		);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - 존재하지 않는 curriculumId로 필터링")
	void findIdsByPeriodAndFilters_NonExistentCurriculumId_EmptyResult() {
		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			START_DATE, END_DATE, null, 999L
		);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - 범위 밖 날짜")
	void findIdsByPeriodAndFilters_OutOfRange_EmptyResult() {
		Instant futureStart = Instant.parse("2025-03-01T00:00:00Z");
		Instant futureEnd = Instant.parse("2025-03-31T23:59:59Z");

		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			futureStart, futureEnd, null, null
		);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - 경계값 테스트 (startDate와 정확히 일치)")
	void findIdsByPeriodAndFilters_BoundaryTest_ExactStartDate() {
		Instant exactDate = Instant.parse("2025-01-10T10:00:00Z");

		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			exactDate, END_DATE, null, null
		);

		// scheduledAt > startDate 조건이므로 정확히 일치하는 경우는 제외됨
		assertThat(result).hasSize(2);
		assertThat(result).containsExactlyInAnyOrder(
			session2.getId(), session3.getId()
		);
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - 경계값 테스트 (endDate와 정확히 일치)")
	void findIdsByPeriodAndFilters_BoundaryTest_ExactEndDate() {
		Instant exactDate = Instant.parse("2025-01-25T16:00:00Z");

		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			START_DATE, exactDate, null, null
		);

		// scheduledAt < endDate 조건이므로 정확히 일치하는 경우는 제외됨
		assertThat(result).hasSize(2);
		assertThat(result).containsExactlyInAnyOrder(
			session1.getId(), session2.getId()
		);
	}

	@Test
	@DisplayName("기간별 세션 ID 조회 - 매우 짧은 기간")
	void findIdsByPeriodAndFilters_VeryShortPeriod_Success() {
		Instant shortStart = Instant.parse("2025-01-19T00:00:00Z");
		Instant shortEnd = Instant.parse("2025-01-21T00:00:00Z");

		List<Long> result = sessionJpaRepository.findIdsByPeriodAndFilters(
			shortStart, shortEnd, null, null
		);

		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(session2.getId());
	}
}



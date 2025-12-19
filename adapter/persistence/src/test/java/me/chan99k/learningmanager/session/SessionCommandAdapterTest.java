package me.chan99k.learningmanager.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.session.entity.SessionEntity;

@DisplayName("SessionCommandAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class SessionCommandAdapterTest {

	private static final Long SESSION_ID = 1L;
	private static final Long COURSE_ID = 10L;
	private static final Long CURRICULUM_ID = 100L;
	private static final String TITLE = "테스트 세션";
	private static final Instant NOW = Instant.now();
	private static final Instant SCHEDULED_AT = NOW.plus(7, ChronoUnit.DAYS);
	private static final Instant SCHEDULED_END_AT = SCHEDULED_AT.plus(2, ChronoUnit.HOURS);
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;
	@Mock
	private JpaSessionRepository jpaRepository;
	private SessionCommandAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new SessionCommandAdapter(jpaRepository);
	}

	@Test
	@DisplayName("[Success] create 메서드가 세션을 저장하고 도메인 객체를 반환한다")
	void test01() {
		Session session = createTestSession();
		SessionEntity savedEntity = createTestSessionEntity();
		when(jpaRepository.save(any(SessionEntity.class))).thenReturn(savedEntity);

		Session result = adapter.create(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(SESSION_ID);
		assertThat(result.getTitle()).isEqualTo(TITLE);
		verify(jpaRepository).save(any(SessionEntity.class));
	}

	@Test
	@DisplayName("[Success] save 메서드가 세션을 저장하고 도메인 객체를 반환한다")
	void test02() {
		Session session = createTestSession();
		SessionEntity savedEntity = createTestSessionEntity();
		when(jpaRepository.save(any(SessionEntity.class))).thenReturn(savedEntity);

		Session result = adapter.save(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(SESSION_ID);
		verify(jpaRepository).save(any(SessionEntity.class));
	}

	@Test
	@DisplayName("[Success] delete 메서드가 세션을 삭제한다")
	void test03() {
		Session session = createTestSession();

		adapter.delete(session);

		verify(jpaRepository).delete(any(SessionEntity.class));
	}

	private Session createTestSession() {
		return Session.reconstitute(
			SESSION_ID, COURSE_ID, CURRICULUM_ID, null,
			TITLE, SCHEDULED_AT, SCHEDULED_END_AT, SessionType.ONLINE, SessionLocation.ZOOM, null,
			List.of(),
			NOW, CREATED_BY, NOW, CREATED_BY, VERSION
		);
	}

	private SessionEntity createTestSessionEntity() {
		SessionEntity entity = new SessionEntity();
		entity.setId(SESSION_ID);
		entity.setCourseId(COURSE_ID);
		entity.setCurriculumId(CURRICULUM_ID);
		entity.setTitle(TITLE);
		entity.setScheduledAt(SCHEDULED_AT);
		entity.setScheduledEndAt(SCHEDULED_END_AT);
		entity.setType(SessionType.ONLINE);
		entity.setLocation(SessionLocation.ZOOM);
		entity.setCreatedAt(NOW);
		entity.setCreatedBy(CREATED_BY);
		entity.setLastModifiedAt(NOW);
		entity.setLastModifiedBy(CREATED_BY);
		entity.setVersion(VERSION);
		entity.setParticipants(List.of());
		entity.setChildren(List.of());
		return entity;
	}
}

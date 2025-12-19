package me.chan99k.learningmanager.session.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionLocation;
import me.chan99k.learningmanager.session.SessionParticipant;
import me.chan99k.learningmanager.session.SessionParticipantRole;
import me.chan99k.learningmanager.session.SessionType;
import me.chan99k.learningmanager.session.entity.SessionEntity;
import me.chan99k.learningmanager.session.entity.SessionParticipantEntity;

@DisplayName("SessionMapper 테스트")
class SessionMapperTest {

	private static final Long SESSION_ID = 1L;
	private static final Long COURSE_ID = 10L;
	private static final Long CURRICULUM_ID = 100L;
	private static final String TITLE = "테스트 세션";
	private static final Instant NOW = Instant.now();
	private static final Instant SCHEDULED_AT = NOW.plus(7, ChronoUnit.DAYS);
	private static final Instant SCHEDULED_END_AT = SCHEDULED_AT.plus(2, ChronoUnit.HOURS);
	private static final SessionType TYPE = SessionType.ONLINE;
	private static final SessionLocation LOCATION = SessionLocation.ZOOM;
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;

	@Nested
	@DisplayName("toEntity")
	class ToEntityTest {

		@Test
		@DisplayName("[Success] 도메인 객체를 엔티티로 변환한다")
		void test01() {
			SessionParticipant participant = SessionParticipant.reconstitute(
				1L, 100L, SessionParticipantRole.HOST,
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);
			Session session = Session.reconstitute(
				SESSION_ID, COURSE_ID, CURRICULUM_ID, null,
				TITLE, SCHEDULED_AT, SCHEDULED_END_AT, TYPE, LOCATION, null,
				List.of(participant),
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);

			SessionEntity entity = SessionMapper.toEntity(session);

			assertThat(entity).isNotNull();
			assertThat(entity.getId()).isEqualTo(SESSION_ID);
			assertThat(entity.getCourseId()).isEqualTo(COURSE_ID);
			assertThat(entity.getCurriculumId()).isEqualTo(CURRICULUM_ID);
			assertThat(entity.getTitle()).isEqualTo(TITLE);
			assertThat(entity.getScheduledAt()).isEqualTo(SCHEDULED_AT);
			assertThat(entity.getScheduledEndAt()).isEqualTo(SCHEDULED_END_AT);
			assertThat(entity.getType()).isEqualTo(TYPE);
			assertThat(entity.getLocation()).isEqualTo(LOCATION);
			assertThat(entity.getParticipants()).hasSize(1);
		}

		@Test
		@DisplayName("[Success] 자식 세션이 있는 경우 함께 변환한다")
		void test02() {
			Session parent = Session.reconstitute(
				SESSION_ID, COURSE_ID, CURRICULUM_ID, null,
				TITLE, SCHEDULED_AT, SCHEDULED_END_AT, TYPE, LOCATION, null,
				List.of(),
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);
			Session child = Session.reconstitute(
				2L, COURSE_ID, CURRICULUM_ID, parent,
				"자식 세션", SCHEDULED_AT, SCHEDULED_END_AT, TYPE, LOCATION, null,
				List.of(),
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);
			parent.setChildren(List.of(child));

			SessionEntity entity = SessionMapper.toEntity(parent);

			assertThat(entity.getChildren()).hasSize(1);
			assertThat(entity.getChildren().get(0).getTitle()).isEqualTo("자식 세션");
			assertThat(entity.getChildren().get(0).getParent()).isEqualTo(entity);
		}
	}

	@Nested
	@DisplayName("toDomain")
	class ToDomainTest {

		@Test
		@DisplayName("[Success] 엔티티를 도메인 객체로 변환한다")
		void test01() {
			SessionEntity entity = new SessionEntity();
			entity.setId(SESSION_ID);
			entity.setCourseId(COURSE_ID);
			entity.setCurriculumId(CURRICULUM_ID);
			entity.setTitle(TITLE);
			entity.setScheduledAt(SCHEDULED_AT);
			entity.setScheduledEndAt(SCHEDULED_END_AT);
			entity.setType(TYPE);
			entity.setLocation(LOCATION);
			entity.setCreatedAt(NOW);
			entity.setCreatedBy(CREATED_BY);
			entity.setLastModifiedAt(NOW);
			entity.setLastModifiedBy(CREATED_BY);
			entity.setVersion(VERSION);
			entity.setChildren(new ArrayList<>());

			SessionParticipantEntity participantEntity = new SessionParticipantEntity();
			participantEntity.setId(1L);
			participantEntity.setSession(entity);
			participantEntity.setMemberId(100L);
			participantEntity.setRole(SessionParticipantRole.HOST);
			participantEntity.setCreatedAt(NOW);
			participantEntity.setCreatedBy(CREATED_BY);
			participantEntity.setLastModifiedAt(NOW);
			participantEntity.setLastModifiedBy(CREATED_BY);
			participantEntity.setVersion(VERSION);
			entity.setParticipants(List.of(participantEntity));

			Session session = SessionMapper.toDomain(entity);

			assertThat(session).isNotNull();
			assertThat(session.getId()).isEqualTo(SESSION_ID);
			assertThat(session.getCourseId()).isEqualTo(COURSE_ID);
			assertThat(session.getCurriculumId()).isEqualTo(CURRICULUM_ID);
			assertThat(session.getTitle()).isEqualTo(TITLE);
			assertThat(session.getScheduledAt()).isEqualTo(SCHEDULED_AT);
			assertThat(session.getScheduledEndAt()).isEqualTo(SCHEDULED_END_AT);
			assertThat(session.getType()).isEqualTo(TYPE);
			assertThat(session.getLocation()).isEqualTo(LOCATION);
			assertThat(session.getParticipants()).hasSize(1);
		}

		@Test
		@DisplayName("[Success] 자식 세션이 있는 경우 함께 변환한다")
		void test02() {
			SessionEntity parentEntity = new SessionEntity();
			parentEntity.setId(SESSION_ID);
			parentEntity.setCourseId(COURSE_ID);
			parentEntity.setTitle(TITLE);
			parentEntity.setScheduledAt(SCHEDULED_AT);
			parentEntity.setScheduledEndAt(SCHEDULED_END_AT);
			parentEntity.setType(TYPE);
			parentEntity.setLocation(LOCATION);
			parentEntity.setCreatedAt(NOW);
			parentEntity.setCreatedBy(CREATED_BY);
			parentEntity.setLastModifiedAt(NOW);
			parentEntity.setLastModifiedBy(CREATED_BY);
			parentEntity.setVersion(VERSION);
			parentEntity.setParticipants(new ArrayList<>());

			SessionEntity childEntity = new SessionEntity();
			childEntity.setId(2L);
			childEntity.setCourseId(COURSE_ID);
			childEntity.setTitle("자식 세션");
			childEntity.setScheduledAt(SCHEDULED_AT);
			childEntity.setScheduledEndAt(SCHEDULED_END_AT);
			childEntity.setType(TYPE);
			childEntity.setLocation(LOCATION);
			childEntity.setParent(parentEntity);
			childEntity.setCreatedAt(NOW);
			childEntity.setCreatedBy(CREATED_BY);
			childEntity.setLastModifiedAt(NOW);
			childEntity.setLastModifiedBy(CREATED_BY);
			childEntity.setVersion(VERSION);
			childEntity.setParticipants(new ArrayList<>());
			childEntity.setChildren(new ArrayList<>());

			parentEntity.setChildren(List.of(childEntity));

			Session session = SessionMapper.toDomain(parentEntity);

			assertThat(session.getChildren()).hasSize(1);
			assertThat(session.getChildren().getFirst().getTitle()).isEqualTo("자식 세션");
			assertThat(session.getChildren().getFirst().getParent()).isEqualTo(session);
		}
	}
}

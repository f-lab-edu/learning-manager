package me.chan99k.learningmanager.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.common.SortOrder;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.dto.SessionInfo;
import me.chan99k.learningmanager.session.entity.SessionEntity;

@DisplayName("SessionQueryAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class SessionQueryAdapterTest {

	private static final Long SESSION_ID = 1L;
	private static final Long COURSE_ID = 10L;
	private static final Long CURRICULUM_ID = 100L;
	private static final Long MEMBER_ID = 1L;
	private static final String TITLE = "테스트 세션";
	private static final Instant NOW = Instant.now();
	private static final Instant SCHEDULED_AT = NOW.plus(7, ChronoUnit.DAYS);
	private static final Instant SCHEDULED_END_AT = SCHEDULED_AT.plus(2, ChronoUnit.HOURS);
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;
	@Mock
	private JpaSessionRepository jpaRepository;
	private SessionQueryAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new SessionQueryAdapter(jpaRepository);
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

	@Nested
	@DisplayName("단순 조회 메서드")
	class SimpleQueryTests {

		@Test
		@DisplayName("[Success] findById로 세션을 조회한다")
		void test01() {
			SessionEntity entity = createTestSessionEntity();
			when(jpaRepository.findById(SESSION_ID)).thenReturn(Optional.of(entity));

			Optional<Session> result = adapter.findById(SESSION_ID);

			assertThat(result).isPresent();
			assertThat(result.get().getId()).isEqualTo(SESSION_ID);
		}

		@Test
		@DisplayName("[Success] findById로 존재하지 않는 세션 조회 시 empty 반환")
		void test02() {
			when(jpaRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

			Optional<Session> result = adapter.findById(SESSION_ID);

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("[Success] findByCourseId로 과정의 세션 목록을 조회한다")
		void test03() {
			SessionEntity entity = createTestSessionEntity();
			when(jpaRepository.findByCourseId(COURSE_ID)).thenReturn(List.of(entity));

			List<Session> result = adapter.findByCourseId(COURSE_ID);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getCourseId()).isEqualTo(COURSE_ID);
		}

		@Test
		@DisplayName("[Success] findByCurriculumId로 커리큘럼의 세션 목록을 조회한다")
		void test04() {
			SessionEntity entity = createTestSessionEntity();
			when(jpaRepository.findByCurriculumId(CURRICULUM_ID)).thenReturn(List.of(entity));

			List<Session> result = adapter.findByCurriculumId(CURRICULUM_ID);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getCurriculumId()).isEqualTo(CURRICULUM_ID);
		}

		@Test
		@DisplayName("[Success] findByParentId로 자식 세션 목록을 조회한다")
		void test05() {
			SessionEntity childEntity = createTestSessionEntity();
			childEntity.setId(2L);
			when(jpaRepository.findByParentId(SESSION_ID)).thenReturn(List.of(childEntity));

			List<Session> result = adapter.findByParentId(SESSION_ID);

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("[Success] findManagedSessionById로 관리 권한이 있는 세션을 조회한다")
		void test06() {
			SessionEntity entity = createTestSessionEntity();
			when(jpaRepository.findManagedSessionById(SESSION_ID, MEMBER_ID, CourseRole.MANAGER))
				.thenReturn(Optional.of(entity));

			Optional<Session> result = adapter.findManagedSessionById(SESSION_ID, MEMBER_ID);

			assertThat(result).isPresent();
		}
	}

	@Nested
	@DisplayName("필터 + 페이징 조회 메서드")
	class FilteredPagingQueryTests {

		@Test
		@DisplayName("[Success] findAllWithFilters로 필터링된 세션 목록을 페이징 조회한다")
		void test01() {
			SessionEntity entity = createTestSessionEntity();
			Page<SessionEntity> page = new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1);
			when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(Pageable.class)))
				.thenReturn(page);
			PageRequest pageRequest = PageRequest.of(0, 10);

			PageResult<Session> result = adapter.findAllWithFilters(
				SessionType.ONLINE, SessionLocation.ZOOM, NOW, SCHEDULED_END_AT, pageRequest
			);

			assertThat(result.content()).hasSize(1);
			assertThat(result.totalElements()).isEqualTo(1);
		}

		@Test
		@DisplayName("[Success] findByCourseIdWithFilters로 과정별 필터링된 세션 목록을 페이징 조회한다")
		void test02() {
			SessionEntity entity = createTestSessionEntity();
			Page<SessionEntity> page = new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1);
			when(jpaRepository.findByCourseIdWithFilters(anyLong(), any(), any(), any(), any(), anyBoolean(),
				any(Pageable.class)))
				.thenReturn(page);
			PageRequest pageRequest = PageRequest.of(0, 10);

			PageResult<Session> result = adapter.findByCourseIdWithFilters(
				COURSE_ID, SessionType.ONLINE, SessionLocation.ZOOM, NOW, SCHEDULED_END_AT, true, pageRequest
			);

			assertThat(result.content()).hasSize(1);
		}

		@Test
		@DisplayName("[Success] findByCurriculumIdWithFilters로 커리큘럼별 필터링된 세션 목록을 페이징 조회한다")
		void test03() {
			SessionEntity entity = createTestSessionEntity();
			Page<SessionEntity> page = new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1);
			when(jpaRepository.findByCurriculumIdWithFilters(anyLong(), any(), any(), any(), any(), anyBoolean(),
				any(Pageable.class)))
				.thenReturn(page);
			PageRequest pageRequest = PageRequest.of(0, 10);

			PageResult<Session> result = adapter.findByCurriculumIdWithFilters(
				CURRICULUM_ID, SessionType.ONLINE, SessionLocation.ZOOM, NOW, SCHEDULED_END_AT, true, pageRequest
			);

			assertThat(result.content()).hasSize(1);
		}

		@Test
		@DisplayName("[Success] findByMemberIdWithFilters로 회원별 필터링된 세션 목록을 페이징 조회한다")
		void test04() {
			SessionEntity entity = createTestSessionEntity();
			Page<SessionEntity> page = new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1);
			when(jpaRepository.findByMemberIdWithFilters(anyLong(), any(), any(), any(), any(), any(Pageable.class)))
				.thenReturn(page);
			PageRequest pageRequest = PageRequest.of(0, 10);

			PageResult<Session> result = adapter.findByMemberIdWithFilters(
				MEMBER_ID, SessionType.ONLINE, SessionLocation.ZOOM, NOW, SCHEDULED_END_AT, pageRequest
			);

			assertThat(result.content()).hasSize(1);
		}

		@Test
		@DisplayName("[Success] 정렬 옵션이 있는 페이지 요청을 처리한다")
		void test05() {
			SessionEntity entity = createTestSessionEntity();
			Page<SessionEntity> page = new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1);
			when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(Pageable.class)))
				.thenReturn(page);
			PageRequest pageRequest = PageRequest.of(0, 10, "scheduledAt", SortOrder.DESC);

			PageResult<Session> result = adapter.findAllWithFilters(
				null, null, null, null, pageRequest
			);

			assertThat(result.content()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("YearMonth 및 ID 조회 메서드")
	class YearMonthAndIdQueryTests {

		@Test
		@DisplayName("[Success] findByYearMonth로 월별 세션 목록을 조회한다")
		void test01() {
			SessionEntity entity = createTestSessionEntity();
			when(jpaRepository.findByYearMonth(any(), any(), any(), any(), any(), any()))
				.thenReturn(List.of(entity));
			YearMonth yearMonth = YearMonth.of(2025, 1);

			List<Session> result = adapter.findByYearMonth(
				yearMonth, SessionType.ONLINE, SessionLocation.ZOOM, COURSE_ID, CURRICULUM_ID
			);

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("[Success] findSessionIdsByPeriodAndFilters로 기간별 세션 ID 목록을 조회한다")
		void test02() {
			when(jpaRepository.findIdsByPeriodAndFilters(any(), any(), any(), any()))
				.thenReturn(List.of(SESSION_ID, 2L, 3L));

			List<Long> result = adapter.findSessionIdsByPeriodAndFilters(NOW, SCHEDULED_END_AT, COURSE_ID,
				CURRICULUM_ID);

			assertThat(result).hasSize(3);
		}

		@Test
		@DisplayName("[Success] findSessionIdsByCourseId로 과정별 세션 ID 목록을 조회한다")
		void test03() {
			SessionEntity entity = createTestSessionEntity();
			when(jpaRepository.findByCourseId(COURSE_ID)).thenReturn(List.of(entity));

			List<Long> result = adapter.findSessionIdsByCourseId(COURSE_ID);

			assertThat(result).hasSize(1);
			assertThat(result.get(0)).isEqualTo(SESSION_ID);
		}

		@Test
		@DisplayName("[Success] findSessionIdsByCurriculumId로 커리큘럼별 세션 ID 목록을 조회한다")
		void test04() {
			SessionEntity entity = createTestSessionEntity();
			when(jpaRepository.findByCurriculumId(CURRICULUM_ID)).thenReturn(List.of(entity));

			List<Long> result = adapter.findSessionIdsByCurriculumId(CURRICULUM_ID);

			assertThat(result).hasSize(1);
			assertThat(result.get(0)).isEqualTo(SESSION_ID);
		}

		@Test
		@DisplayName("[Success] findSessionIdsByMemberId로 회원별 세션 ID 목록을 조회한다")
		void test05() {
			SessionEntity entity = createTestSessionEntity();
			Page<SessionEntity> page = new PageImpl<>(List.of(entity));
			when(
				jpaRepository.findByMemberIdWithFilters(eq(MEMBER_ID), any(), any(), any(), any(), any(Pageable.class)))
				.thenReturn(page);

			List<Long> result = adapter.findSessionIdsByMemberId(MEMBER_ID);

			assertThat(result).hasSize(1);
			assertThat(result.get(0)).isEqualTo(SESSION_ID);
		}

		@Test
		@DisplayName("[Success] findSessionIdsByMonthAndFilters로 월별 세션 ID 목록을 조회한다")
		void test06() {
			when(jpaRepository.findIdsByPeriodAndFilters(any(), any(), any(), any()))
				.thenReturn(List.of(SESSION_ID));

			List<Long> result = adapter.findSessionIdsByMonthAndFilters(2025, 1, COURSE_ID, CURRICULUM_ID);

			assertThat(result).hasSize(1);
		}
	}

	@Nested
	@DisplayName("SessionInfo 조회 메서드")
	class SessionInfoQueryTests {

		@Test
		@DisplayName("[Success] findSessionInfoProjectionByIds로 세션 정보를 조회한다")
		void test01() {
			SessionInfo sessionInfo = new SessionInfo(SESSION_ID, TITLE, SCHEDULED_AT, COURSE_ID, "과정명", CURRICULUM_ID,
				"커리큘럼명");
			when(jpaRepository.findSessionInfoProjectionByIds(List.of(SESSION_ID)))
				.thenReturn(List.of(sessionInfo));

			List<SessionInfo> result = adapter.findSessionInfoProjectionByIds(List.of(SESSION_ID));

			assertThat(result).hasSize(1);
			assertThat(result.get(0).sessionId()).isEqualTo(SESSION_ID);
		}

		@Test
		@DisplayName("[Success] 빈 ID 목록으로 findSessionInfoProjectionByIds 호출 시 빈 목록 반환")
		void test02() {
			List<SessionInfo> result = adapter.findSessionInfoProjectionByIds(List.of());

			assertThat(result).isEmpty();
			verify(jpaRepository, never()).findSessionInfoProjectionByIds(any());
		}

		@Test
		@DisplayName("[Success] findSessionInfoByIds로 세션 정보를 조회한다")
		void test03() {
			SessionInfo sessionInfo = new SessionInfo(SESSION_ID, TITLE, SCHEDULED_AT, COURSE_ID, "과정명", CURRICULUM_ID,
				"커리큘럼명");
			when(jpaRepository.findSessionInfoProjectionByIds(List.of(SESSION_ID)))
				.thenReturn(List.of(sessionInfo));

			List<SessionInfo> result = adapter.findSessionInfoByIds(List.of(SESSION_ID));

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("[Success] findSessionInfoMapByIds로 세션 정보 맵을 조회한다")
		void test04() {
			SessionInfo sessionInfo = new SessionInfo(SESSION_ID, TITLE, SCHEDULED_AT, COURSE_ID, "과정명", CURRICULUM_ID,
				"커리큘럼명");
			when(jpaRepository.findSessionInfoProjectionByIds(List.of(SESSION_ID)))
				.thenReturn(List.of(sessionInfo));

			Map<Long, SessionInfo> result = adapter.findSessionInfoMapByIds(List.of(SESSION_ID));

			assertThat(result).hasSize(1);
			assertThat(result).containsKey(SESSION_ID);
			assertThat(result.get(SESSION_ID).sessionTitle()).isEqualTo(TITLE);
		}
	}
}

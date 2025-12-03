package me.chan99k.learningmanager.application.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.common.SortOrder;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionListRetrieval;
import me.chan99k.learningmanager.session.SessionListService;
import me.chan99k.learningmanager.session.SessionLocation;
import me.chan99k.learningmanager.session.SessionQueryRepository;
import me.chan99k.learningmanager.session.SessionType;

@ExtendWith(MockitoExtension.class)
class SessionListServiceTest {

	@Mock
	private SessionQueryRepository sessionQueryRepository;

	@Mock
	private Clock clock;

	@InjectMocks
	private SessionListService sessionListService;

	@BeforeEach
	void setUp() {
		Instant fixedInstant = Instant.parse("2024-03-15T12:00:00Z");
		lenient().when(clock.instant()).thenReturn(fixedInstant);
		lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
	}

	@Test
	@DisplayName("전체 세션 목록 조회 - 성공")
	void getSessionList_Success() {
		// given
		var session = createMockSession(1L, "테스트 세션", SessionType.ONLINE, SessionLocation.ZOOM);
		var pageRequest = PageRequest.of(0, 20, "scheduledAt", SortOrder.DESC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(sessionQueryRepository.findAllWithFilters(any(), any(), any(), any(), any(PageRequest.class)))
			.thenReturn(sessions);

		var request = new SessionListRetrieval.SessionListRequest(0, 20, "scheduledAt,desc", null, null, null, null);

		// when
		PageResult<SessionListRetrieval.SessionListResponse> result = sessionListService.getSessionList(request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(1L);
		assertThat(result.content().get(0).title()).isEqualTo("테스트 세션");

		verify(sessionQueryRepository).findAllWithFilters(
			eq(null), eq(null), eq(null), eq(null),
			argThat(pr -> pr.page() == 0 &&
				pr.size() == 20 &&
				"scheduledAt".equals(pr.sortBy()) &&
				pr.sortOrder() == SortOrder.DESC)
		);
	}

	@Test
	@DisplayName("과정별 세션 목록 조회 - 성공")
	void getCourseSessionList_Success() {
		// given
		Long courseId = 100L;
		var session = createMockSessionWithCustomValues(1L, "과정 세션", SessionType.OFFLINE, SessionLocation.SITE,
			courseId, null);
		var pageRequest = PageRequest.of(0, 10, "title", SortOrder.ASC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(sessionQueryRepository.findByCourseIdWithFilters(any(), any(), any(), any(), any(), any(),
			any(PageRequest.class)))
			.thenReturn(sessions);

		var request = new SessionListRetrieval.CourseSessionListRequest(
			0, 10, "title,asc", SessionType.OFFLINE, null, null, null, true
		);

		// when
		PageResult<SessionListRetrieval.SessionListResponse> result =
			sessionListService.getCourseSessionList(courseId, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).type()).isEqualTo(SessionType.OFFLINE);

		verify(sessionQueryRepository).findByCourseIdWithFilters(
			eq(courseId), eq(SessionType.OFFLINE), eq(null), eq(null), eq(null), eq(true),
			argThat(pr -> pr.page() == 0 &&
				pr.size() == 10 &&
				"title".equals(pr.sortBy()) &&
				pr.sortOrder() == SortOrder.ASC)
		);
	}

	@Test
	@DisplayName("커리큘럼별 세션 목록 조회 - 성공")
	void getCurriculumSessionList_Success() {
		// given
		Long curriculumId = 200L;
		var session = createMockSessionWithCustomValues(1L, "커리큘럼 세션", SessionType.ONLINE, SessionLocation.GOOGLE_MEET,
			null, curriculumId);
		var pageRequest = PageRequest.of(1, 5, "scheduledAt", SortOrder.ASC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(sessionQueryRepository.findByCurriculumIdWithFilters(any(), any(), any(), any(), any(), any(),
			any(PageRequest.class)))
			.thenReturn(sessions);

		var request = new SessionListRetrieval.CurriculumSessionListRequest(
			1, 5, "scheduledAt,asc", null, SessionLocation.GOOGLE_MEET, null, null, false
		);

		// when
		PageResult<SessionListRetrieval.SessionListResponse> result =
			sessionListService.getCurriculumSessionList(curriculumId, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).location()).isEqualTo(SessionLocation.GOOGLE_MEET);

		verify(sessionQueryRepository).findByCurriculumIdWithFilters(
			eq(curriculumId), eq(null), eq(SessionLocation.GOOGLE_MEET), eq(null), eq(null), eq(false),
			argThat(pr -> pr.page() == 1 &&
				pr.size() == 5 &&
				"scheduledAt".equals(pr.sortBy()) &&
				pr.sortOrder() == SortOrder.ASC)
		);
	}

	@Test
	@DisplayName("세션 상태 결정 - 예정")
	void determineSessionStatus_Upcoming() {
		// given
		Instant futureStart = Instant.now().plusSeconds(3600);
		Instant futureEnd = futureStart.plusSeconds(7200);
		var session = createMockSessionWithTime(1L, "미래 세션", futureStart, futureEnd);
		var pageRequest = PageRequest.of(0, 20, "scheduledAt", SortOrder.DESC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(sessionQueryRepository.findAllWithFilters(any(), any(), any(), any(), any(PageRequest.class)))
			.thenReturn(sessions);

		var request = new SessionListRetrieval.SessionListRequest(0, 20, "scheduledAt,desc", null, null, null, null);

		// when
		PageResult<SessionListRetrieval.SessionListResponse> result = sessionListService.getSessionList(request);

		// then
		assertThat(result.content().get(0).status()).isEqualTo(SessionListRetrieval.SessionStatus.UPCOMING);
	}

	@Test
	@DisplayName("세션 상태 결정 - 완료")
	void determineSessionStatus_Completed() {
		// given - clock의 고정된 시간을 기준으로 과거 시간 설정
		Instant fixedNow = clock.instant();
		Instant pastStart = fixedNow.minusSeconds(7200);
		Instant pastEnd = fixedNow.minusSeconds(3600);
		var session = createMockSessionWithTime(1L, "과거 세션", pastStart, pastEnd);
		var pageRequest = PageRequest.of(0, 20, "scheduledAt", SortOrder.DESC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(sessionQueryRepository.findAllWithFilters(any(), any(), any(), any(), any(PageRequest.class)))
			.thenReturn(sessions);

		var request = new SessionListRetrieval.SessionListRequest(0, 20, "scheduledAt,desc", null, null, null, null);

		// when
		PageResult<SessionListRetrieval.SessionListResponse> result = sessionListService.getSessionList(request);

		// then
		assertThat(result.content().get(0).status()).isEqualTo(SessionListRetrieval.SessionStatus.COMPLETED);
	}

	@Test
	@DisplayName("세션 상태 결정 - 진행 중")
	void determineSessionStatus_Ongoing() {
		// given - clock의 고정된 시간을 기준으로 진행 중 시간 설정
		Instant fixedNow = clock.instant();
		Instant ongoingStart = fixedNow.minusSeconds(3600);
		Instant ongoingEnd = fixedNow.plusSeconds(3600);
		var session = createMockSessionWithTime(1L, "진행 중 세션", ongoingStart, ongoingEnd);
		var pageRequest = PageRequest.of(0, 20, "scheduledAt", SortOrder.DESC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(sessionQueryRepository.findAllWithFilters(any(), any(), any(), any(), any(PageRequest.class)))
			.thenReturn(sessions);

		var request = new SessionListRetrieval.SessionListRequest(0, 20, "scheduledAt,desc", null, null, null, null);

		// when
		PageResult<SessionListRetrieval.SessionListResponse> result = sessionListService.getSessionList(request);

		// then
		assertThat(result.content().get(0).status()).isEqualTo(SessionListRetrieval.SessionStatus.ONGOING);
	}

	@Test
	@DisplayName("페이징 파라미터 정규화 - 잘못된 값들")
	void normalizeRequestParameters() {
		// given
		var session = createMockSession(1L, "테스트", SessionType.ONLINE, SessionLocation.ZOOM);
		var pageRequest = PageRequest.of(0, 20, "scheduledAt", SortOrder.DESC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(sessionQueryRepository.findAllWithFilters(any(), any(), any(), any(), any(PageRequest.class)))
			.thenReturn(sessions);

		// 잘못된 파라미터들 (-1 page, 0 size, null sort)
		var request = new SessionListRetrieval.SessionListRequest(-1, 0, null, null, null, null, null);

		// when
		sessionListService.getSessionList(request);

		// then - 정규화된 값들로 호출되었는지 확인
		verify(sessionQueryRepository).findAllWithFilters(
			any(), any(), any(), any(),
			argThat(pr -> pr.page() == 0 && // -1 -> 0
				pr.size() == 20) // 0 -> 20
		);
	}

	@Test
	@DisplayName("사용자별 세션 목록 조회 - 성공")
	void getUserSessionList_Success() {
		// given
		Long memberId = 300L;
		var session = createMockSession(1L, "사용자 세션", SessionType.ONLINE, SessionLocation.ZOOM);
		var pageRequest = PageRequest.of(0, 20, "scheduledAt", SortOrder.DESC);
		var sessions = PageResult.of(List.of(session), pageRequest, 1);

		when(
			sessionQueryRepository.findByMemberIdWithFilters(any(), any(), any(), any(), any(), any(PageRequest.class)))
			.thenReturn(sessions);

		var request = new SessionListRetrieval.UserSessionListRequest(
			0, 20, "scheduledAt,desc", null, null, null, null
		);

		// when
		PageResult<SessionListRetrieval.SessionListResponse> result =
			sessionListService.getUserSessionList(memberId, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(1L);
		assertThat(result.content().get(0).title()).isEqualTo("사용자 세션");

		verify(sessionQueryRepository).findByMemberIdWithFilters(
			eq(memberId), eq(null), eq(null), eq(null), eq(null),
			argThat(pr -> pr.page() == 0 &&
				pr.size() == 20 &&
				"scheduledAt".equals(pr.sortBy()) &&
				pr.sortOrder() == SortOrder.DESC)
		);
	}

	@Test
	@DisplayName("세션 캘린더 조회 - 성공")
	void getSessionCalendar_Success() {
		// given
		YearMonth yearMonth = YearMonth.of(2024, 1);
		LocalDate sessionDate = LocalDate.of(2024, 1, 15);
		Instant sessionDateTime = sessionDate.atStartOfDay().toInstant(ZoneOffset.UTC);

		var session = createMockSessionWithTime(1L, "캘린더 세션",
			sessionDateTime, sessionDateTime.plusSeconds(3600));

		when(sessionQueryRepository.findByYearMonth(any(), any(), any(), any(), any()))
			.thenReturn(List.of(session));

		var request = new SessionListRetrieval.SessionCalendarRequest(
			null, null, null, null
		);

		// when
		Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>> result =
			sessionListService.getSessionCalendar(yearMonth, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result).containsKey(sessionDate);
		assertThat(result.get(sessionDate)).hasSize(1);
		assertThat(result.get(sessionDate).get(0).id()).isEqualTo(1L);
		assertThat(result.get(sessionDate).get(0).title()).isEqualTo("캘린더 세션");

		verify(sessionQueryRepository).findByYearMonth(
			eq(yearMonth), eq(null), eq(null), eq(null), eq(null)
		);
	}

	@Test
	@DisplayName("세션 캘린더 조회 - 필터 적용")
	void getSessionCalendar_WithFilters() {
		// given
		YearMonth yearMonth = YearMonth.of(2024, 2);
		Long courseId = 100L;
		SessionType sessionType = SessionType.OFFLINE;

		when(sessionQueryRepository.findByYearMonth(any(), any(), any(), any(), any()))
			.thenReturn(List.of());

		var request = new SessionListRetrieval.SessionCalendarRequest(
			sessionType, SessionLocation.SITE, courseId, null
		);

		// when
		Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>> result =
			sessionListService.getSessionCalendar(yearMonth, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();

		verify(sessionQueryRepository).findByYearMonth(
			eq(yearMonth), eq(sessionType), eq(SessionLocation.SITE), eq(courseId), eq(null)
		);
	}

	private Session createMockSession(Long id, String title, SessionType type, SessionLocation location) {
		return createMockSessionWithTime(id, title, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
	}

	private Session createMockSessionWithTime(Long id, String title, Instant scheduledAt, Instant scheduledEndAt) {
		Session session = mock(Session.class, withSettings().lenient());
		when(session.getId()).thenReturn(id);
		when(session.getTitle()).thenReturn(title);
		when(session.getScheduledAt()).thenReturn(scheduledAt);
		when(session.getScheduledEndAt()).thenReturn(scheduledEndAt);
		when(session.getType()).thenReturn(SessionType.ONLINE);
		when(session.getLocation()).thenReturn(SessionLocation.ZOOM);
		when(session.getLocationDetails()).thenReturn(null);
		when(session.getCourseId()).thenReturn(100L);
		when(session.getCurriculumId()).thenReturn(200L);
		when(session.getParent()).thenReturn(null);
		when(session.getChildren()).thenReturn(List.of());
		when(session.getParticipants()).thenReturn(List.of());
		return session;
	}

	private Session createMockSessionWithCustomValues(Long id, String title, SessionType type, SessionLocation location,
		Long courseId, Long curriculumId) {
		Session session = mock(Session.class, withSettings().lenient());
		when(session.getId()).thenReturn(id);
		when(session.getTitle()).thenReturn(title);
		when(session.getScheduledAt()).thenReturn(Instant.now().plusSeconds(3600));
		when(session.getScheduledEndAt()).thenReturn(Instant.now().plusSeconds(7200));
		when(session.getType()).thenReturn(type);
		when(session.getLocation()).thenReturn(location);
		when(session.getLocationDetails()).thenReturn(null);
		when(session.getCourseId()).thenReturn(courseId);
		when(session.getCurriculumId()).thenReturn(curriculumId);
		when(session.getParent()).thenReturn(null);
		when(session.getChildren()).thenReturn(List.of());
		when(session.getParticipants()).thenReturn(List.of());
		return session;
	}
}
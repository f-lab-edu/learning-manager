package me.chan99k.learningmanager.domain.study;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SessionTest {

	private Curriculum curriculum;
	private Instant now;

	@BeforeEach
	void setUp() {
		curriculum = null;
		now = Instant.now();
	}

	@Nested
	@DisplayName("루트 세션 생성 테스트")
	class CreateRootSession {

		@Test
		@DisplayName("[Success] 유효한 정보로 루트 세션 생성에 성공한다.")
		void create_root_session_success() {
			String title = "JPA 스터디";
			Instant startTime = now;
			Instant endTime = now.plus(2, ChronoUnit.HOURS);

			Session session = Session.createRootSession(curriculum, title, startTime, endTime, SessionType.ONLINE,
				SessionLocation.GOOGLE_MEET, null);

			assertThat(session).isNotNull();
			assertThat(session.getTitle()).isEqualTo(title);
			assertThat(session.isRootSession()).isTrue();
		}

		@Test
		@DisplayName("[Success] 세션 진행 시간이 24시간 미만이면 성공적으로 생성된다.")
		void create_duration_edge_case_success() {
			// 시작 시간을 자정으로 설정하여 날짜가 넘어가는 사이드 이펙트를 방지
			Instant startTime = now.truncatedTo(ChronoUnit.DAYS);
			Instant endTime = startTime.plus(23, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES);

			Session session = Session.createRootSession(curriculum, "23시간 59분 세션", startTime, endTime,
				SessionType.ONLINE,
				SessionLocation.GOOGLE_MEET, null);

			assertThat(session).isNotNull();
		}

		@Test
		@DisplayName("[Failure] 시작 시간이 종료 시간보다 늦으면 예외가 발생한다.")
		void create_fail_due_to_invalid_time_order() {
			Instant startTime = now;
			Instant endTime = now.minus(1, ChronoUnit.SECONDS);

			assertThatThrownBy(() -> {
				Session.createRootSession(curriculum, "잘못된 시간", startTime, endTime, SessionType.ONLINE,
					SessionLocation.GOOGLE_MEET, null);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("세션 시작 시간은 종료 시간보다 빨라야 합니다.");
		}

		@Test
		@DisplayName("[Failure] 세션이 이틀에 걸쳐 진행되면 예외가 발생한다.")
		void create_fail_due_to_crossing_day_boundary() {
			// 23시에 시작해서 2시간 뒤인 다음날 01시에 끝나는 세션
			Instant startTime = now.truncatedTo(ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS);
			Instant endTime = startTime.plus(2, ChronoUnit.HOURS);

			assertThatThrownBy(() -> {
				Session.createRootSession(curriculum, "이틀 걸친 세션", startTime, endTime, SessionType.ONLINE,
					SessionLocation.ZOOM, null);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("세션은 이틀에 걸쳐 분포할 수 없습니다.");
		}

		@Test
		@DisplayName("[Failure] 오프라인 세션에 상세 장소가 없으면 예외가 발생한다.")
		void create_fail_due_to_missing_location_details() {
			Instant startTime = now;
			Instant endTime = now.plus(2, ChronoUnit.HOURS);

			assertThatThrownBy(() -> {
				Session.createRootSession(curriculum, "오프라인 스터디", startTime, endTime, SessionType.OFFLINE,
					SessionLocation.SITE, null);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("오프라인 세션은 상세 장소 설명이 필수입니다.");
		}

		@Test
		@DisplayName("[Failure] 세션 진행 시간이 24시간이거나 그 이상이면 예외가 발생한다.")
		void create_fail_due_to_duration_exceeding_24_hours() {
			Instant startTime = now;
			Instant endTime = now.plus(24, ChronoUnit.HOURS);

			assertThatThrownBy(() -> {
				Session.createRootSession(curriculum, "24시간 세션", startTime, endTime, SessionType.ONLINE,
					SessionLocation.ZOOM, null);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("세션은 24시간을 초과할 수 없습니다.");
		}
	}

	@Nested
	@DisplayName("하위 세션 생성 테스트")
	class CreateSubSession {

		private Session rootSession;

		@BeforeEach
		void createRootSession() {
			rootSession = Session.createRootSession(curriculum, "메인 세션", now, now.plus(8, ChronoUnit.HOURS),
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);
		}

		@Test
		@DisplayName("[Success] 유효한 루트 세션 아래에 하위 세션 생성에 성공한다.")
		void create_sub_session_success() {
			String subTitle = "Q&A 세션";
			Instant subStartTime = now.plus(1, ChronoUnit.HOURS);
			Instant subEndTime = now.plus(2, ChronoUnit.HOURS);

			Session subSession = Session.createSubSession(rootSession, subTitle, subStartTime, subEndTime,
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);

			assertThat(subSession).isNotNull();
			assertThat(subSession.isChildSession()).isTrue();
			assertThat(subSession.getParent()).isEqualTo(rootSession);
			assertThat(rootSession.getChildren()).contains(subSession);
		}

		@Test
		@DisplayName("[Failure] 하위 세션 아래에 또 다른 하위 세션을 생성하면 예외가 발생한다.")
		void create_fail_due_to_invalid_depth() {
			Session subSession = Session.createSubSession(rootSession, "1차 하위 세션", now.plus(1, ChronoUnit.HOURS),
				now.plus(2, ChronoUnit.HOURS), SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);

			assertThatThrownBy(() -> {
				Session.createSubSession(subSession, "2차 하위 세션", now.plus(1, ChronoUnit.HOURS),
					now.plus(2, ChronoUnit.HOURS), SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("하위 세션은 또 다른 하위 세션을 가질 수 없습니다.");
		}

		@Test
		@DisplayName("[Failure] 하위 세션의 시간이 부모 세션의 범위를 벗어나면 예외가 발생한다.")
		void create_fail_due_to_time_range_violation() {
			Instant subStartTime = now.plus(1, ChronoUnit.HOURS);
			Instant subEndTime = now.plus(9, ChronoUnit.HOURS);

			assertThatThrownBy(() -> {
				Session.createSubSession(rootSession, "시간 초과 세션", subStartTime, subEndTime,
					SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("하위 세션의 종료 시간은 부모 세션의 종료 시간보다 늦을 수 없습니다.");
		}
	}

	@Nested
	@DisplayName("세션 정보 수정 테스트")
	class UpdateSession {

		private Session session;

		@BeforeEach
		void setUp() {
			session = Session.createRootSession(null, "초기 제목", Instant.now(), Instant.now().plus(2, ChronoUnit.HOURS),
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);
		}

		@Test
		@DisplayName("[Success] 유효한 정보로 세션 내용을 성공적으로 수정한다.")
		void update_session_success() {
			String newTitle = "수정된 제목";
			Instant newStartTime = Instant.now().plus(1, ChronoUnit.DAYS);
			Instant newEndTime = newStartTime.plus(3, ChronoUnit.HOURS);

			session.update(newTitle, newStartTime, newEndTime, SessionType.OFFLINE, SessionLocation.SITE, "강남역 스터디룸");

			assertThat(session.getTitle()).isEqualTo(newTitle);
			assertThat(session.getScheduledAt()).isEqualTo(newStartTime);
			assertThat(session.getLocation()).isEqualTo(SessionLocation.SITE);
			assertThat(session.getLocationDetails()).isEqualTo("강남역 스터디룸");
		}

		@Test
		@DisplayName("[Failure] 수정하려는 시간이 제약조건을 위반하면 예외가 발생한다.")
		void update_fail_due_to_invalid_time() {
			String newTitle = "수정된 제목";
			Instant newStartTime = Instant.now().plus(1, ChronoUnit.DAYS);
			Instant newEndTime = newStartTime.minus(1, ChronoUnit.SECONDS);

			assertThatThrownBy(() -> {
				session.update(newTitle, newStartTime, newEndTime, SessionType.ONLINE, SessionLocation.GOOGLE_MEET,
					null);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("세션 시작 시간은 종료 시간보다 빨라야 합니다.");
		}
	}
}
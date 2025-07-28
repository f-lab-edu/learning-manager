package me.chan99k.learningmanager.domain.session;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SessionTest {

	private Instant now;

	@BeforeEach
	void setUp() {
		now = Instant.now();
	}

	@Nested
	@DisplayName("세션 생성 테스트")
	class SessionCreationTest {

		@Test
		@DisplayName("[Success] 독립 세션(Standalone) 생성에 성공한다.")
		void create_standalone_session_success() {
			String title = "독립 스터디";
			Instant startTime = now;
			Instant endTime = now.plus(2, ChronoUnit.HOURS);

			Session session = Session.createStandaloneSession(title, startTime, endTime, SessionType.ONLINE,
				SessionLocation.GOOGLE_MEET, null);

			assertThat(session).isNotNull();
			assertThat(session.getTitle()).isEqualTo(title);
			assertThat(session.isRootSession()).isTrue();
			assertThat(session.getCourseId()).isNull();
			assertThat(session.getCurriculumId()).isNull();
		}

		@Test
		@DisplayName("[Success] 코스 소속 세션 생성에 성공한다.")
		void create_course_session_success() {
			Long courseId = 1L;
			Session session = Session.createCourseSession(courseId, "코스 전체 특강", now, now.plus(2, ChronoUnit.HOURS),
				SessionType.ONLINE, SessionLocation.ZOOM, null);

			assertThat(session).isNotNull();
			assertThat(session.getCourseId()).isEqualTo(courseId);
			assertThat(session.getCurriculumId()).isNull();
		}

		@Test
		@DisplayName("[Success] 커리큘럼 소속 세션 생성에 성공한다.")
		void create_curriculum_session_success() {
			Long courseId = 1L;
			Long curriculumId = 10L;
			Session session = Session.createCurriculumSession(courseId, curriculumId, "커리큘럼 정규 세션", now,
				now.plus(2, ChronoUnit.HOURS), SessionType.ONLINE, SessionLocation.ZOOM, null);

			assertThat(session).isNotNull();
			assertThat(session.getCourseId()).isEqualTo(courseId);
			assertThat(session.getCurriculumId()).isEqualTo(curriculumId);
		}

		@Test
		@DisplayName("[Success] 세션 진행 시간이 24시간 미만이면 성공적으로 생성된다.")
		void create_duration_edge_case_success() {
			Instant startTime = now.truncatedTo(ChronoUnit.DAYS);
			Instant endTime = startTime.plus(23, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES);

			Session session = Session.createStandaloneSession("23시간 59분 세션", startTime, endTime,
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);

			assertThat(session).isNotNull();
		}

		@Test
		@DisplayName("[Failure] 시작 시간이 종료 시간보다 늦으면 예외가 발생한다.")
		void create_fail_due_to_invalid_time_order() {
			Instant startTime = now;
			Instant endTime = now.minus(1, ChronoUnit.SECONDS);

			assertThatThrownBy(() -> Session.createStandaloneSession("잘못된 시간", startTime, endTime, SessionType.ONLINE,
				SessionLocation.GOOGLE_MEET, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 세션 시작 시간은 종료 시간보다 빨라야 합니다.");
		}

		@Test
		@DisplayName("[Failure] 세션이 이틀에 걸쳐 진행되면 예외가 발생한다.")
		void create_fail_due_to_crossing_day_boundary() {
			Instant startTime = now.truncatedTo(ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS);
			Instant endTime = startTime.plus(2, ChronoUnit.HOURS);

			assertThatThrownBy(() -> Session.createStandaloneSession("이틀 걸친 세션", startTime, endTime, SessionType.ONLINE,
				SessionLocation.ZOOM, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 세션은 이틀에 걸쳐 진행될 수 없습니다.");
		}

		@Test
		@DisplayName("[Failure] 오프라인 세션에 상세 장소가 없으면 예외가 발생한다.")
		void create_fail_due_to_missing_location_details() {
			assertThatThrownBy(() -> Session.createStandaloneSession("오프라인 스터디", now, now.plus(2, ChronoUnit.HOURS),
				SessionType.OFFLINE, SessionLocation.SITE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 오프라인 세션은 상세 장소 설명이 필수입니다.");
		}

		@Test
		@DisplayName("[Failure] 세션 진행 시간이 24시간이거나 그 이상이면 예외가 발생한다.")
		void create_fail_due_to_duration_exceeding_24_hours() {
			Instant startTime = now;
			Instant endTime = now.plus(24, ChronoUnit.HOURS);

			assertThatThrownBy(() -> Session.createStandaloneSession("24시간 세션", startTime, endTime, SessionType.ONLINE,
				SessionLocation.ZOOM, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 세션은 24시간을 초과할 수 없습니다.");
		}
	}

	@Nested
	@DisplayName("세션 계층 구조 테스트")
	class SessionHierarchyTest {

		private Session rootSession;

		@BeforeEach
		void createRootSession() {
			// 하위 세션을 테스트하기 위한 부모 세션 생성
			rootSession = Session.createStandaloneSession("메인 세션", now, now.plus(8, ChronoUnit.HOURS),
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);
		}

		@Test
		@DisplayName("[Success] 유효한 루트 세션 아래에 하위 세션 생성에 성공한다.")
		void create_child_session_success() {
			String subTitle = "Q&A 세션";
			Instant subStartTime = now.plus(1, ChronoUnit.HOURS);
			Instant subEndTime = now.plus(2, ChronoUnit.HOURS);

			// 위에서 제안한 createChildSession 메서드를 사용
			Session childSession = rootSession.createChildSession(subTitle, subStartTime, subEndTime,
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);

			assertThat(childSession).isNotNull();
			assertThat(childSession.isChildSession()).isTrue();
			assertThat(childSession.getParent()).isEqualTo(rootSession);
			assertThat(rootSession.getChildren()).contains(childSession);
		}

		@Test
		@DisplayName("[Failure] 하위 세션 아래에 또 다른 하위 세션을 생성하면 예외가 발생한다.")
		void create_fail_due_to_invalid_depth() {
			Session childSession = rootSession.createChildSession("1차 하위 세션", now.plus(1, ChronoUnit.HOURS),
				now.plus(2, ChronoUnit.HOURS), SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);

			assertThatThrownBy(() -> childSession.createChildSession("2차 하위 세션", now.plus(1, ChronoUnit.HOURS),
				now.plus(2, ChronoUnit.HOURS), SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 하위 세션은 또 다른 하위 세션을 가질 수 없습니다.");
		}

		@Test
		@DisplayName("[Failure] 하위 세션의 시간이 부모 세션의 범위를 벗어나면 예외가 발생한다.")
		void create_fail_due_to_time_range_violation() {
			Instant subStartTime = now.plus(1, ChronoUnit.HOURS);
			Instant subEndTime = now.plus(9, ChronoUnit.HOURS); // 부모 종료 시간(8시간 뒤)보다 늦음

			assertThatThrownBy(() -> rootSession.createChildSession("시간 초과 세션", subStartTime, subEndTime,
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 하위 세션의 종료 시간은 부모 세션의 종료 시간보다 늦을 수 없습니다.");
		}
	}

	@Nested
	@DisplayName("세션 참여자 관리 테스트")
	class ParticipantManagementTest {
		private Session session;

		@BeforeEach
		void setUp() {
			session = Session.createStandaloneSession("참여자 관리 테스트 세션", now, now.plus(1, ChronoUnit.HOURS),
				SessionType.ONLINE, SessionLocation.ZOOM, null);
		}

		@Test
		@DisplayName("[Success] 세션에 참여자를 성공적으로 추가한다.")
		void add_participant_success() {
			Long memberId = 1L;
			session.addParticipant(memberId, SessionParticipantRole.SPEAKER);

			assertThat(session.getParticipants()).hasSize(1);
			assertThat(session.getParticipants().get(0).getMemberId()).isEqualTo(memberId);
			assertThat(session.getParticipants().get(0).getRole()).isEqualTo(SessionParticipantRole.SPEAKER);
		}

		@Test
		@DisplayName("[Failure] 동일한 참여자를 중복으로 추가하면 예외가 발생한다.")
		void add_duplicate_participant_fail() {
			Long memberId = 1L;
			session.addParticipant(memberId, SessionParticipantRole.HOST);

			assertThatThrownBy(() -> session.addParticipant(memberId, SessionParticipantRole.HOST))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 이미 세션에 참여 중인 멤버입니다.");
		}

		@Test
		@DisplayName("[Success] 세션에서 참여자를 성공적으로 제거한다.")
		void remove_participant_success() {
			Long memberId1 = 1L;
			Long memberId2 = 2L;
			session.addParticipant(memberId1, SessionParticipantRole.ATTENDEE);
			session.addParticipant(memberId2, SessionParticipantRole.ATTENDEE);

			session.removeParticipant(memberId1);

			assertThat(session.getParticipants()).hasSize(1);
			assertThat(session.getParticipants().get(0).getMemberId()).isEqualTo(memberId2);
		}
	}

	@Nested
	@DisplayName("세션 정보 수정 테스트")
	class UpdateSessionTest {

		private Session session;

		@BeforeEach
		void setUp() {
			Instant futureStartTime = now.plus(4, ChronoUnit.DAYS);
			Instant futureEndTime = futureStartTime.plus(2, ChronoUnit.HOURS);

			session = Session.createStandaloneSession("충분히 미래인 세션", futureStartTime, futureEndTime,
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);
		}

		@Test
		@DisplayName("[Success] 유효한 정보로 세션 내용을 성공적으로 수정한다.")
		void update_session_success() {
			String newTitle = "수정된 제목";
			Instant newStartTime = now.plus(1, ChronoUnit.DAYS);
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
			Instant newStartTime = now.plus(1, ChronoUnit.DAYS);
			Instant newEndTime = newStartTime.minus(1, ChronoUnit.SECONDS);

			assertThatThrownBy(() -> session.update(newTitle, newStartTime, newEndTime, SessionType.ONLINE,
				SessionLocation.GOOGLE_MEET, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("[System] 세션 시작 시간은 종료 시간보다 빨라야 합니다.");
		}
	}
}
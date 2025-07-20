package me.chan99k.learningmanager.domain.study;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CurriculumTest {

	private Course course;
	private Curriculum curriculum;

	@BeforeEach
	void setUp() {
		course = Course.create("테스트 스터디", "설명");
		curriculum = Curriculum.create(course, "초기 커리큘럼", "JPA 기초");
	}

	@Nested
	@DisplayName("커리큘럼 생성 및 수정 테스트")
	class CreateAndUpdate {

		@Test
		@DisplayName("[Success] 유효한 정보로 커리큘럼을 성공적으로 생성한다.")
		void create_curriculum_success() {
			String title = "새로운 커리큘럼";
			String description = "Spring 기초";

			Curriculum newCurriculum = Curriculum.create(course, title, description);

			assertThat(newCurriculum).isNotNull();
			assertThat(newCurriculum.getCourse()).isEqualTo(course);
			assertThat(newCurriculum.getTitle()).isEqualTo(title);
			assertThat(newCurriculum.getDescription()).isEqualTo(description);
			assertThat(newCurriculum.getSessionList()).isEmpty();
		}

		@Test
		@DisplayName("[Success] 커리큘럼의 제목과 설명을 성공적으로 수정한다.")
		void update_curriculum_success() {
			String newTitle = "수정된 커리큘럼";
			String newDescription = "Spring 심화";

			curriculum.update(newTitle, newDescription);

			assertThat(curriculum.getTitle()).isEqualTo(newTitle);
			assertThat(curriculum.getDescription()).isEqualTo(newDescription);
		}
	}

	@Nested
	@DisplayName("커리큘럼 세션 관리 테스트")
	class ManageSessions {

		private Session session1;
		private Session session2;

		@BeforeEach
		void setUp() {
			Instant now = Instant.now();
			session1 = Session.createRootSession(curriculum, "세션 1", now, now.plus(2, ChronoUnit.HOURS),
				SessionType.ONLINE, SessionLocation.GOOGLE_MEET, null);
			session2 = Session.createRootSession(curriculum, "세션 2", now.plus(1, ChronoUnit.DAYS),
				now.plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS), SessionType.OFFLINE, SessionLocation.SITE,
				"강남역 스타벅스");
		}

		@Test
		@DisplayName("[Success] 커리큘럼에 새로운 세션을 성공적으로 추가한다.")
		void add_session_success() {
			curriculum.addSession(session1);

			assertThat(curriculum.getSessionList()).hasSize(1);
			assertThat(curriculum.getSessionList()).contains(session1);
		}

		@Test
		@DisplayName("[Success] 커리큘럼에서 특정 세션을 성공적으로 제외한다.")
		void detach_session_success() {
			curriculum.addSession(session1);
			curriculum.addSession(session2);
			assertThat(curriculum.getSessionList()).hasSize(2);

			curriculum.detachSessionFromCurriculum(session1);

			assertThat(curriculum.getSessionList()).hasSize(1);
			assertThat(curriculum.getSessionList()).doesNotContain(session1);
			assertThat(curriculum.getSessionList()).contains(session2);
		}
	}
}

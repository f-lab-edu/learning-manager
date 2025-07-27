package me.chan99k.learningmanager.domain.study;

import static org.assertj.core.api.Assertions.*;

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
}

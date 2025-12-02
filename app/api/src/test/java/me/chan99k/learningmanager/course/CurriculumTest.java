package me.chan99k.learningmanager.course;

import static me.chan99k.learningmanager.course.CourseProblemCode.*;
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
		@DisplayName("[Success] 커리큘럼의 제목을 성공적으로 수정한다.")
		void update_curriculum_success() {
			String newTitle = "수정된 커리큘럼 제목";

			curriculum.updateTitle(newTitle);

			assertThat(curriculum.getTitle()).isEqualTo(newTitle);
		}

		@Test
		@DisplayName("[Success] 커리큘럼의 설명을 성공적으로 수정한다.")
		void update_curriculum_description_success() {
			String newDescription = "수정된 커리큘럼 설명";

			curriculum.updateDescription(newDescription);

			assertThat(curriculum.getDescription()).isEqualTo(newDescription);
		}

		@Test
		@DisplayName("[Failure] 커리큘럼 제목이 비어있으면 수정에 실패한다")
		void fail_to_update_course_when_title_is_null() {

			assertThatThrownBy(() -> curriculum.updateTitle(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(CURRICULUM_TITLE_REQUIRED.getMessage());
		}

		@Test
		@DisplayName("[Failure] 커리큘럼 설명이 비어있으면 수정에 실패한다")
		void fail_to_update_course_when_description_is_null() {

			assertThatThrownBy(() -> curriculum.updateDescription(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(CURRICULUM_DESCRIPTION_REQUIRED.getMessage());
		}
	}
}

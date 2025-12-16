package me.chan99k.learningmanager.course.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.chan99k.learningmanager.course.Course;
import me.chan99k.learningmanager.course.CourseMember;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.course.Curriculum;
import me.chan99k.learningmanager.course.entity.CourseEntity;
import me.chan99k.learningmanager.course.entity.CourseMemberEntity;
import me.chan99k.learningmanager.course.entity.CurriculumEntity;

@DisplayName("CourseMapper 테스트")
class CourseMapperTest {

	private static final Long COURSE_ID = 1L;
	private static final String TITLE = "테스트 과정";
	private static final String DESCRIPTION = "과정 설명";
	private static final Instant NOW = Instant.now();
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;

	@Nested
	@DisplayName("toEntity")
	class ToEntityTest {

		@Test
		@DisplayName("[Success] 도메인 객체를 엔티티로 변환한다")
		void test01() {
			CourseMember courseMember = CourseMember.reconstitute(
				1L, 100L, CourseRole.MANAGER,
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);
			Curriculum curriculum = Curriculum.reconstitute(
				1L, "커리큘럼 제목", "커리큘럼 설명",
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);
			Course course = Course.reconstitute(
				COURSE_ID, TITLE, DESCRIPTION,
				List.of(courseMember), List.of(curriculum),
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);

			CourseEntity entity = CourseMapper.toEntity(course);

			assertThat(entity).isNotNull();
			assertThat(entity.getId()).isEqualTo(COURSE_ID);
			assertThat(entity.getTitle()).isEqualTo(TITLE);
			assertThat(entity.getDescription()).isEqualTo(DESCRIPTION);
			assertThat(entity.getCourseMemberList()).hasSize(1);
			assertThat(entity.getCurriculumList()).hasSize(1);
		}

		@Test
		@DisplayName("[Edge] null 입력 시 null을 반환한다")
		void test02() {
			CourseEntity entity = CourseMapper.toEntity(null);

			assertThat(entity).isNull();
		}
	}

	@Nested
	@DisplayName("toDomain")
	class ToDomainTest {

		@Test
		@DisplayName("[Success] 엔티티를 도메인 객체로 변환한다")
		void test01() {
			CourseEntity entity = new CourseEntity();
			entity.setId(COURSE_ID);
			entity.setTitle(TITLE);
			entity.setDescription(DESCRIPTION);
			entity.setCreatedAt(NOW);
			entity.setCreatedBy(CREATED_BY);
			entity.setLastModifiedAt(NOW);
			entity.setLastModifiedBy(CREATED_BY);
			entity.setVersion(VERSION);

			CourseMemberEntity memberEntity = new CourseMemberEntity();
			memberEntity.setId(1L);
			memberEntity.setCourse(entity);
			memberEntity.setMemberId(100L);
			memberEntity.setCourseRole(CourseRole.MANAGER);
			memberEntity.setCreatedAt(NOW);
			memberEntity.setCreatedBy(CREATED_BY);
			memberEntity.setLastModifiedAt(NOW);
			memberEntity.setLastModifiedBy(CREATED_BY);
			memberEntity.setVersion(VERSION);
			entity.setCourseMemberList(List.of(memberEntity));

			CurriculumEntity curriculumEntity = new CurriculumEntity();
			curriculumEntity.setId(1L);
			curriculumEntity.setCourse(entity);
			curriculumEntity.setTitle("커리큘럼 제목");
			curriculumEntity.setDescription("커리큘럼 설명");
			curriculumEntity.setCreatedAt(NOW);
			curriculumEntity.setCreatedBy(CREATED_BY);
			curriculumEntity.setLastModifiedAt(NOW);
			curriculumEntity.setLastModifiedBy(CREATED_BY);
			curriculumEntity.setVersion(VERSION);
			entity.setCurriculumList(List.of(curriculumEntity));

			Course course = CourseMapper.toDomain(entity);

			assertThat(course).isNotNull();
			assertThat(course.getId()).isEqualTo(COURSE_ID);
			assertThat(course.getTitle()).isEqualTo(TITLE);
			assertThat(course.getDescription()).isEqualTo(DESCRIPTION);
			assertThat(course.getCourseMemberList()).hasSize(1);
			assertThat(course.getCurriculumList()).hasSize(1);
		}

		@Test
		@DisplayName("[Edge] null 입력 시 null을 반환한다")
		void test02() {
			Course course = CourseMapper.toDomain(null);

			assertThat(course).isNull();
		}
	}
}

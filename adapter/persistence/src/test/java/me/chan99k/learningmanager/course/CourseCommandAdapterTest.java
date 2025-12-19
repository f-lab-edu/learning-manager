package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.entity.CourseEntity;

@DisplayName("CourseCommandAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class CourseCommandAdapterTest {

	private static final Long COURSE_ID = 1L;
	private static final String TITLE = "테스트 과정";
	private static final String DESCRIPTION = "과정 설명";
	private static final Instant NOW = Instant.now();
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;
	@Mock
	private JpaCourseRepository jpaCourseRepository;
	private CourseCommandAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new CourseCommandAdapter(jpaCourseRepository);
	}

	@Test
	@DisplayName("[Success] create 메서드가 과정을 저장하고 도메인 객체를 반환한다")
	void test01() {
		Course course = createTestCourse();
		CourseEntity savedEntity = createTestCourseEntity();
		when(jpaCourseRepository.save(any(CourseEntity.class))).thenReturn(savedEntity);

		Course result = adapter.create(course);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(COURSE_ID);
		assertThat(result.getTitle()).isEqualTo(TITLE);
		verify(jpaCourseRepository).save(any(CourseEntity.class));
	}

	@Test
	@DisplayName("[Success] save 메서드가 과정을 저장하고 도메인 객체를 반환한다")
	void test02() {
		Course course = createTestCourse();
		CourseEntity savedEntity = createTestCourseEntity();
		when(jpaCourseRepository.save(any(CourseEntity.class))).thenReturn(savedEntity);

		Course result = adapter.save(course);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(COURSE_ID);
		verify(jpaCourseRepository).save(any(CourseEntity.class));
	}

	@Test
	@DisplayName("[Success] delete 메서드가 과정을 삭제한다")
	void test03() {
		Course course = createTestCourse();

		adapter.delete(course);

		verify(jpaCourseRepository).delete(any(CourseEntity.class));
	}

	private Course createTestCourse() {
		return Course.reconstitute(
			COURSE_ID, TITLE, DESCRIPTION,
			List.of(), List.of(),
			NOW, CREATED_BY, NOW, CREATED_BY, VERSION
		);
	}

	private CourseEntity createTestCourseEntity() {
		CourseEntity entity = new CourseEntity();
		entity.setId(COURSE_ID);
		entity.setTitle(TITLE);
		entity.setDescription(DESCRIPTION);
		entity.setCreatedAt(NOW);
		entity.setCreatedBy(CREATED_BY);
		entity.setLastModifiedAt(NOW);
		entity.setLastModifiedBy(CREATED_BY);
		entity.setVersion(VERSION);
		entity.setCourseMemberList(List.of());
		entity.setCurriculumList(List.of());
		return entity;
	}
}

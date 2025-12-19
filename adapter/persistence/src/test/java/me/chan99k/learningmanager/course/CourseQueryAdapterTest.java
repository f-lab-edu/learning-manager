package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
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
import me.chan99k.learningmanager.course.entity.CourseEntity;

@DisplayName("CourseQueryAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class CourseQueryAdapterTest {

	private static final Long COURSE_ID = 1L;
	private static final Long MEMBER_ID = 1L;
	private static final String TITLE = "테스트 과정";
	private static final String DESCRIPTION = "과정 설명";
	private static final Instant NOW = Instant.now();
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;
	@Mock
	private JpaCourseRepository jpaCourseRepository;
	private CourseQueryAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new CourseQueryAdapter(jpaCourseRepository);
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

	@Nested
	@DisplayName("단순 조회 메서드")
	class SimpleQueryTests {

		@Test
		@DisplayName("[Success] findById로 과정을 조회한다")
		void test01() {
			CourseEntity entity = createTestCourseEntity();
			when(jpaCourseRepository.findById(COURSE_ID)).thenReturn(Optional.of(entity));

			Optional<Course> result = adapter.findById(COURSE_ID);

			assertThat(result).isPresent();
			assertThat(result.get().getId()).isEqualTo(COURSE_ID);
		}

		@Test
		@DisplayName("[Success] findById로 존재하지 않는 과정 조회 시 empty 반환")
		void test02() {
			when(jpaCourseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

			Optional<Course> result = adapter.findById(COURSE_ID);

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("[Success] findByTitle로 과정을 조회한다")
		void test03() {
			CourseEntity entity = createTestCourseEntity();
			when(jpaCourseRepository.findByTitle(TITLE)).thenReturn(Optional.of(entity));

			Optional<Course> result = adapter.findByTitle(TITLE);

			assertThat(result).isPresent();
			assertThat(result.get().getTitle()).isEqualTo(TITLE);
		}

		@Test
		@DisplayName("[Success] findManagedCourseById로 관리 권한이 있는 과정을 조회한다")
		void test04() {
			CourseEntity entity = createTestCourseEntity();
			when(jpaCourseRepository.findManagedCourseById(COURSE_ID, MEMBER_ID))
				.thenReturn(Optional.of(entity));

			Optional<Course> result = adapter.findManagedCourseById(COURSE_ID, MEMBER_ID);

			assertThat(result).isPresent();
		}

		@Test
		@DisplayName("[Success] findManagedCoursesByMemberId로 관리 중인 과정 목록을 조회한다")
		void test05() {
			CourseEntity entity = createTestCourseEntity();
			when(jpaCourseRepository.findManagedCoursesByMemberId(MEMBER_ID))
				.thenReturn(List.of(entity));

			List<Course> result = adapter.findManagedCoursesByMemberId(MEMBER_ID);

			assertThat(result).hasSize(1);
		}
	}

	@Nested
	@DisplayName("상세 정보 및 페이징 조회 메서드")
	class DetailAndPagingQueryTests {

		@Test
		@DisplayName("[Success] findCourseDetailById로 과정 상세 정보를 조회한다")
		void test01() {
			CourseDetailInfo detailInfo = new CourseDetailInfo(COURSE_ID, TITLE, DESCRIPTION, NOW, 10L, 5L);
			when(jpaCourseRepository.findCourseBasicDetailsById(COURSE_ID))
				.thenReturn(Optional.of(detailInfo));

			Optional<CourseDetailInfo> result = adapter.findCourseDetailById(COURSE_ID);

			assertThat(result).isPresent();
			assertThat(result.get().courseId()).isEqualTo(COURSE_ID);
		}

		@Test
		@DisplayName("[Success] findCourseMembersByCourseId로 과정 멤버를 페이징 조회한다")
		void test02() {
			CourseMemberInfo memberInfo = new CourseMemberInfo(MEMBER_ID, "닉네임", "test@example.com", CourseRole.MANAGER,
				NOW);
			Page<CourseMemberInfo> page = new PageImpl<>(List.of(memberInfo), Pageable.ofSize(10), 1);
			when(jpaCourseRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(Pageable.class)))
				.thenReturn(page);
			PageRequest pageRequest = PageRequest.of(0, 10);

			PageResult<CourseMemberInfo> result = adapter.findCourseMembersByCourseId(COURSE_ID, pageRequest);

			assertThat(result.content()).hasSize(1);
			assertThat(result.totalElements()).isEqualTo(1);
		}

		@Test
		@DisplayName("[Success] 정렬 옵션이 있는 페이지 요청을 처리한다")
		void test03() {
			CourseMemberInfo memberInfo = new CourseMemberInfo(MEMBER_ID, "닉네임", "test@example.com", CourseRole.MANAGER,
				NOW);
			Page<CourseMemberInfo> page = new PageImpl<>(List.of(memberInfo), Pageable.ofSize(10), 1);
			when(jpaCourseRepository.findCourseMembersByCourseId(eq(COURSE_ID), any(Pageable.class)))
				.thenReturn(page);
			PageRequest pageRequest = PageRequest.of(0, 10, "nickname", SortOrder.ASC);

			PageResult<CourseMemberInfo> result = adapter.findCourseMembersByCourseId(COURSE_ID, pageRequest);

			assertThat(result.content()).hasSize(1);
		}
	}
}

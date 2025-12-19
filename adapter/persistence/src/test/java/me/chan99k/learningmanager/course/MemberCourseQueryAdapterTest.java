package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;
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
import me.chan99k.learningmanager.member.CourseParticipationInfo;

@DisplayName("MemberCourseQueryAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class MemberCourseQueryAdapterTest {

	private static final Long COURSE_ID = 1L;
	private static final Long MEMBER_ID = 1L;
	private static final String TITLE = "테스트 과정";
	private static final String DESCRIPTION = "과정 설명";
	private static final Instant NOW = Instant.now();
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;
	@Mock
	private JpaCourseRepository jpaCourseRepository;
	private MemberCourseQueryAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new MemberCourseQueryAdapter(jpaCourseRepository);
	}

	@Test
	@DisplayName("[Success] findManagedCoursesByMemberId로 관리 중인 과정 목록을 조회한다")
	void test01() {
		CourseEntity entity = createTestCourseEntity();
		when(jpaCourseRepository.findManagedCoursesByMemberId(MEMBER_ID))
			.thenReturn(List.of(entity));

		List<Course> result = adapter.findManagedCoursesByMemberId(MEMBER_ID);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(COURSE_ID);
	}

	@Test
	@DisplayName("[Success] findParticipatingCoursesByMemberId로 참여 중인 과정 목록을 조회한다")
	void test02() {
		CourseEntity entity = createTestCourseEntity();
		when(jpaCourseRepository.findParticipatingCoursesByMemberId(MEMBER_ID))
			.thenReturn(List.of(entity));

		List<Course> result = adapter.findParticipatingCoursesByMemberId(MEMBER_ID);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(COURSE_ID);
	}

	@Test
	@DisplayName("[Success] findParticipatingCoursesWithRoleByMemberId로 역할과 함께 참여 중인 과정 목록을 조회한다")
	void test03() {
		CourseParticipationInfo participationInfo = new CourseParticipationInfo(COURSE_ID, TITLE, DESCRIPTION,
			CourseRole.MENTEE);
		when(jpaCourseRepository.findParticipatingCoursesWithRoleByMemberId(MEMBER_ID))
			.thenReturn(List.of(participationInfo));

		List<CourseParticipationInfo> result = adapter.findParticipatingCoursesWithRoleByMemberId(MEMBER_ID);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).courseId()).isEqualTo(COURSE_ID);
		assertThat(result.get(0).role()).isEqualTo(CourseRole.MENTEE);
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

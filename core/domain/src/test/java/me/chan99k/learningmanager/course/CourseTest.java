package me.chan99k.learningmanager.course;

import static me.chan99k.learningmanager.course.CourseProblemCode.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CourseTest {

	private Course course;

	@BeforeEach
	void setUp() {
		course = Course.create("수정전 스터디", "스터디 설명");
	}

	@Nested
	@DisplayName("과정 생성 및 수정 테스트")
	class CreateAndUpdate {

		@Test
		@DisplayName("[Success] 유효한 정보로 과정을 성공적으로 생성한다.")
		void create_course_success() {
			String title = "새로운 스터디";
			String description = "Spring 기초 스터디입니다.";

			Course newCourse = Course.create(title, description);

			assertThat(newCourse).isNotNull();
			assertThat(newCourse.getTitle()).isEqualTo(title);
			assertThat(newCourse.getDescription()).isEqualTo(description);
			assertThat(newCourse.getCourseMemberList()).isEmpty();
			assertThat(newCourse.getCurriculumList()).isEmpty();
		}

		@Test
		@DisplayName("[Success] 과정의 제목을 성공적으로 수정한다.")
		void update_title_success() {
			String newTitle = "수정된 스터디 과정 제목";

			course.updateTitle(newTitle);

			assertThat(course.getTitle()).isEqualTo(newTitle);
		}

		@Test
		@DisplayName("[Success] 과정의 설명을 성공적으로 수정한다.")
		void update_description_success() {
			String newDescription = "새로운 설명";

			course.updateDescription(newDescription);

			assertThat(course.getDescription()).isEqualTo(newDescription);
		}

		@Test
		@DisplayName("[Failure] 과정 제목이 비어있으면 수정에 실패한다")
		void fail_to_update_course_when_title_is_null() {

			assertThatThrownBy(() -> course.updateTitle(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(COURSE_TITLE_REQUIRED.getMessage());
		}

		@Test
		@DisplayName("[Failure] 과정 설명이 비어있으면 수정에 실패한다")
		void fail_to_update_course_when_description_is_null() {

			assertThatThrownBy(() -> course.updateDescription(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(COURSE_DESCRIPTION_REQUIRED.getMessage());
		}

		@Test
		@DisplayName("[Success] getCourseMemberList는 수정 불가능한 리스트를 반환한다.")
		void getCourseMemberList_returns_unmodifiable_list() {
			course.addMember(1L, CourseRole.MENTEE);
			List<CourseMember> memberList = course.getCourseMemberList();

			// 외부에서 리스트를 수정하려고 할 때 예외가 발생하는지 검증
			assertThatThrownBy(() -> memberList.add(null))
				.isInstanceOf(UnsupportedOperationException.class);
		}
	}

	@Nested
	@DisplayName("과정 멤버 관리 테스트")
	class ManageMembers {

		@Test
		@DisplayName("[Success] 과정에 새로운 멘티를 성공적으로 추가한다.")
		void add_member_mentee_success() {
			Long memberId = 1L;
			CourseRole role = CourseRole.MENTEE;

			course.addMember(memberId, role);

			assertThat(course.getCourseMemberList()).hasSize(1);
			CourseMember addedMember = course.getCourseMemberList().get(0);
			assertThat(addedMember.getMemberId()).isEqualTo(memberId);
			assertThat(addedMember.getCourseRole()).isEqualTo(role);
			assertThat(addedMember.getCourse()).isEqualTo(course);
		}

		@Test
		@DisplayName("[Success] 과정에 새로운 멘토를 성공적으로 추가한다.")
		void add_member_mentor_success() {
			Long memberId = 1L;
			CourseRole role = CourseRole.MENTOR;

			course.addMember(memberId, role);

			assertThat(course.getCourseMemberList()).hasSize(1);
			CourseMember addedMember = course.getCourseMemberList().get(0);
			assertThat(addedMember.getMemberId()).isEqualTo(memberId);
			assertThat(addedMember.getCourseRole()).isEqualTo(role);
			assertThat(addedMember.getCourse()).isEqualTo(course);
		}

		@Test
		@DisplayName("[Success] 과정에 새로운 매니저를 성공적으로 추가한다.")
		void add_member_manager_success() {
			Long memberId = 1L;
			CourseRole role = CourseRole.MANAGER;

			course.addMember(memberId, role);

			assertThat(course.getCourseMemberList()).hasSize(1);
			CourseMember addedMember = course.getCourseMemberList().get(0);
			assertThat(addedMember.getMemberId()).isEqualTo(memberId);
			assertThat(addedMember.getCourseRole()).isEqualTo(role);
			assertThat(addedMember.getCourse()).isEqualTo(course);
		}

		@Test
		@DisplayName("[Success] 과정에 존재하는 멤버를 성공적으로 제외한다.")
		void remove_member_success() {
			Long memberIdToRemove = 1L;
			course.addMember(memberIdToRemove, CourseRole.MENTEE);
			course.addMember(2L, CourseRole.MENTOR);

			assertThat(course.getCourseMemberList()).hasSize(2);

			course.removeMember(memberIdToRemove);

			assertThat(course.getCourseMemberList()).hasSize(1);
			assertThat(course.getCourseMemberList().get(0).getMemberId()).isEqualTo(2L);
		}

		@Test
		@DisplayName("[Failure] 과정에 존재하지 않는 멤버를 제외하려 하면 예외가 발생한다.")
		void remove_non_existing_member_fail() {
			course.addMember(1L, CourseRole.MENTEE);
			assertThat(course.getCourseMemberList()).hasSize(1);

			// 존재하지 않는 ID로 제거를 시도하면 예외가 발생하는 것을 검증합니다.
			assertThatThrownBy(() -> course.removeMember(99L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(COURSE_MEMBER_NOT_REGISTERED.getMessage());
		}
	}

	@Nested
	@DisplayName("과정 커리큘럼 관리 테스트")
	class ManageCurriculum {

		@Test
		@DisplayName("[Success] 과정에 새로운 커리큘럼을 성공적으로 추가한다.")
		void add_curriculum_success() {
			String curriculumTitle = "1주차: JPA 시작하기";
			String curriculumDescription = "엔티티 매핑과 영속성 컨텍스트에 대해 학습합니다.";

			course.addCurriculum(curriculumTitle, curriculumDescription);

			assertThat(course.getCurriculumList()).hasSize(1);
			Curriculum addedCurriculum = course.getCurriculumList().get(0);

			assertThat(addedCurriculum.getTitle()).isEqualTo(curriculumTitle);
			assertThat(addedCurriculum.getDescription()).isEqualTo(curriculumDescription);
			assertThat(addedCurriculum.getCourse()).isEqualTo(course); // 연관관계 확인
		}

		@Test
		@DisplayName("[Success] 과정에 존재하는 커리큘럼을 성공적으로 제거한다.")
		void remove_curriculum_success() {
			course.addCurriculum("1주차", "JPA 기초");
			course.addCurriculum("2주차", "JPA 심화");
			assertThat(course.getCurriculumList()).hasSize(2);

			Curriculum curriculumToRemove = course.getCurriculumList().get(0);

			course.removeCurriculum(curriculumToRemove);

			assertThat(course.getCurriculumList()).hasSize(1);
			assertThat(course.getCurriculumList()).doesNotContain(curriculumToRemove);
			assertThat(course.getCurriculumList().get(0).getTitle()).isEqualTo("2주차");
		}

		@Test
		@DisplayName("[Failure] 과정에 속하지 않은 커리큘럼을 제거하려 하면 예외가 발생한다.")
		void remove_non_existing_curriculum_fail() {
			course.addCurriculum("1주차", "JPA 기초");

			// 다른 과정에 속한 별개의 커리큘럼 생성
			Course anotherCourse = Course.create("다른 과정", "설명");
			Curriculum anoutherCoursesCurriculum = Curriculum.create(anotherCourse, "다른 커리큘럼", "설명");

			// 테스트를 위해 ID를 임의로 설정.
			ReflectionTestUtils.setField(anoutherCoursesCurriculum, "id", 99L);

			assertThatThrownBy(() -> course.removeCurriculum(anoutherCoursesCurriculum))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(CURRICULUM_NOT_FOUND_IN_COURSE.getMessage() + " ID: 99");
		}

		@Test
		@DisplayName("[Failure] null 커리큘럼을 제거하려 하면 예외가 발생한다.")
		void remove_null_curriculum_fail() {
			assertThatThrownBy(() -> course.removeCurriculum(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(CURRICULUM_NULL.getMessage());
		}

		@Test
		@DisplayName("[Success] getCurriculumList는 수정 불가능한 리스트를 반환한다.")
		void getCurriculumList_returns_unmodifiable_list() {
			course.addCurriculum("Test Title", "Test Desc");
			List<Curriculum> curriculumList = course.getCurriculumList();

			// 외부에서 리스트를 수정하려고 할 때 예외가 발생하는지 검증
			assertThatThrownBy(() -> curriculumList.add(null))
				.isInstanceOf(UnsupportedOperationException.class);
		}
	}
}

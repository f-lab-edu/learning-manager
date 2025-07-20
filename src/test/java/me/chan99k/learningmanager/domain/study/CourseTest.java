package me.chan99k.learningmanager.domain.study;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CourseTest {

	private Course course;

	@BeforeEach
	void setUp() {
		course = Course.create("수정전 스터디", "JPA 기초 스터디입니다.");
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
		@DisplayName("[Success] 과정의 제목과 설명을 성공적으로 수정한다.")
		void update_course_success() {
			String newTitle = "수정된 스터디";
			String newDescription = "Spring 심화 스터디입니다.";

			course.update(newTitle, newDescription);

			assertThat(course.getTitle()).isEqualTo(newTitle);
			assertThat(course.getDescription()).isEqualTo(newDescription);
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
		@DisplayName("[Success] 과정에 존재하지 않는 멤버를 제외해도 아무 일도 일어나지 않는다.") // TODO :: 존재하지 않는다고 메시지를 던져줘야 할지 생각해보기
		void remove_non_existing_member_success() {
			course.addMember(1L, CourseRole.MENTEE);
			assertThat(course.getCourseMemberList()).hasSize(1);

			course.removeMember(99L); // 존재하지 않는 ID

			assertThat(course.getCourseMemberList()).hasSize(1);
		}
	}
}

package me.chan99k.learningmanager.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class CourseDetailServiceTest {

	@Mock
	private CourseQueryRepository courseQueryRepository;

	@InjectMocks
	private CourseDetailService courseDetailService;

	@Test
	@DisplayName("[Success] 과정 상세 정보를 성공적으로 조회한다")
	void getCourseDetail_Success() {
		Long courseId = 1L;
		CourseDetailInfo courseDetailInfo = new CourseDetailInfo(
			courseId, "Spring Boot 기초", "스프링 부트 학습 과정",
			Instant.now(), 10L, 5L);

		given(courseQueryRepository.findCourseDetailById(courseId))
			.willReturn(Optional.of(courseDetailInfo));

		CourseDetailRetrieval.CourseDetailResponse response = courseDetailService.getCourseDetail(courseId);

		assertThat(response.courseDetail()).isEqualTo(courseDetailInfo);
		verify(courseQueryRepository).findCourseDetailById(courseId);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 과정 조회 시 예외가 발생한다")
	void getCourseDetail_CourseNotFound() {
		Long courseId = 999L;
		given(courseQueryRepository.findCourseDetailById(courseId))
			.willReturn(Optional.empty());

		assertThatThrownBy(() -> courseDetailService.getCourseDetail(courseId))
			.isInstanceOf(DomainException.class);
	}

	@Test
	@DisplayName("[Success] 과정 멤버 목록을 페이징으로 조회한다")
	void getCourseMembers_Success() {
		Long courseId = 1L;
		PageRequest pageRequest = PageRequest.of(0, 10);
		Course course = Course.create("Test Course", "Description");
		PageResult<CourseMemberInfo> memberPage = PageResult.of(java.util.List.of(
			new CourseMemberInfo(1L, "사용자1", "user1@test.com", CourseRole.MENTEE, Instant.now()),
			new CourseMemberInfo(2L, "사용자2", "user2@test.com", CourseRole.MANAGER, Instant.now())
		), pageRequest, 2);

		given(courseQueryRepository.findById(courseId)).willReturn(Optional.of(course));
		given(courseQueryRepository.findCourseMembersByCourseId(courseId, pageRequest))
			.willReturn(memberPage);

		PageResult<CourseMemberInfo> result = courseDetailService.getCourseMembers(courseId, pageRequest);

		assertThat(result.content()).hasSize(2);
		assertThat(result.content().get(0).nickname()).isEqualTo("사용자1");
		assertThat(result.content().get(1).courseRole()).isEqualTo(CourseRole.MANAGER);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 과정의 멤버 조회 시 예외가 발생한다")
	void getCourseMembers_CourseNotFound() {
		Long courseId = 999L;
		PageRequest pageRequest = PageRequest.of(0, 10);
		given(courseQueryRepository.findById(courseId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> courseDetailService.getCourseMembers(courseId, pageRequest))
			.isInstanceOf(DomainException.class);
	}
}

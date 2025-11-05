package me.chan99k.learningmanager.application.course;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import me.chan99k.learningmanager.application.course.provides.CourseDetailRetrieval;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseRole;
import me.chan99k.learningmanager.domain.exception.DomainException;

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
		Pageable pageable = PageRequest.of(0, 10);
		Course course = Course.create("Test Course", "Description");
		Page<CourseMemberInfo> memberPage = new PageImpl<>(java.util.List.of(
			new CourseMemberInfo(1L, "사용자1", "user1@test.com", CourseRole.MENTEE, Instant.now()),
			new CourseMemberInfo(2L, "사용자2", "user2@test.com", CourseRole.MANAGER, Instant.now())
		));

		given(courseQueryRepository.findById(courseId)).willReturn(Optional.of(course));
		given(courseQueryRepository.findCourseMembersByCourseId(courseId, pageable))
			.willReturn(memberPage);

		Page<CourseMemberInfo> result = courseDetailService.getCourseMembers(courseId, pageable);

		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).nickname()).isEqualTo("사용자1");
		assertThat(result.getContent().get(1).courseRole()).isEqualTo(CourseRole.MANAGER);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 과정의 멤버 조회 시 예외가 발생한다")
	void getCourseMembers_CourseNotFound() {
		Long courseId = 999L;
		Pageable pageable = PageRequest.of(0, 10);
		given(courseQueryRepository.findById(courseId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> courseDetailService.getCourseMembers(courseId, pageable))
			.isInstanceOf(DomainException.class);
	}
}
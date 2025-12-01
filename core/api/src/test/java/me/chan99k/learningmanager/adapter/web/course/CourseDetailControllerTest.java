package me.chan99k.learningmanager.adapter.web.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import me.chan99k.learningmanager.application.course.CourseDetailInfo;
import me.chan99k.learningmanager.application.course.CourseMemberInfo;
import me.chan99k.learningmanager.application.course.provides.CourseDetailRetrieval;
import me.chan99k.learningmanager.domain.course.CourseRole;

@ExtendWith(MockitoExtension.class)
class CourseDetailControllerTest {

	@Mock
	private CourseDetailRetrieval courseDetailRetrieval;

	@Test
	@DisplayName("[Success] 과정 상세 정보 조회가 정상 동작한다")
	void getCourseDetail() throws Exception {
		Long courseId = 1L;
		CourseDetailInfo courseDetail = new CourseDetailInfo(
			courseId, "Spring Boot 기초", "스프링 부트 학습 과정",
			Instant.now(), 10L, 5L);

		CourseDetailRetrieval.CourseDetailResponse response =
			new CourseDetailRetrieval.CourseDetailResponse(courseDetail);

		given(courseDetailRetrieval.getCourseDetail(courseId)).willReturn(response);

		CourseDetailController controller = new CourseDetailController(courseDetailRetrieval, new SyncTaskExecutor());
		ResponseEntity<CourseDetailRetrieval.CourseDetailResponse> result =
			controller.getCourseDetail(courseId).get();

		assertThat(result.getStatusCode().value()).isEqualTo(200);
		Assertions.assertNotNull(result.getBody());
		assertThat(result.getBody().courseDetail()).isEqualTo(courseDetail);
		verify(courseDetailRetrieval).getCourseDetail(courseId);
	}

	@Test
	@DisplayName("[Success] 과정 멤버 목록 조회가 정상 동작한다")
	void getCourseMembers() throws Exception {
		Long courseId = 1L;
		Pageable pageable = PageRequest.of(0, 20);
		List<CourseMemberInfo> members = List.of(
			new CourseMemberInfo(1L, "사용자1", "user1@test.com", CourseRole.MENTEE, Instant.now()),
			new CourseMemberInfo(2L, "사용자2", "user2@test.com", CourseRole.MANAGER, Instant.now())
		);
		Page<CourseMemberInfo> memberPage = new PageImpl<>(members, pageable, 2);

		given(courseDetailRetrieval.getCourseMembers(courseId, pageable))
			.willReturn(memberPage);

		CourseDetailController controller = new CourseDetailController(courseDetailRetrieval, new SyncTaskExecutor());
		ResponseEntity<Page<CourseMemberInfo>> result =
			controller.getCourseMembers(courseId, pageable).get();

		assertThat(result.getStatusCode().value()).isEqualTo(200);
		Assertions.assertNotNull(result.getBody());
		assertThat(result.getBody().getContent()).hasSize(2);
		assertThat(result.getBody().getContent().get(0).nickname()).isEqualTo("사용자1");
		assertThat(result.getBody().getTotalElements()).isEqualTo(2);
		verify(courseDetailRetrieval).getCourseMembers(courseId, pageable);
	}
}
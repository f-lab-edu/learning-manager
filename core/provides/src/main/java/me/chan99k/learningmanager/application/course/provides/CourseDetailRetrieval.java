package me.chan99k.learningmanager.application.course.provides;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.chan99k.learningmanager.application.course.CourseDetailInfo;
import me.chan99k.learningmanager.application.course.CourseMemberInfo;

/**
 * 과정 상세 정보 조회 포트
 * - 과정 기본 정보 + 집계 데이터 조회
 * - 과정 멤버 목록 페이징 조회
 * - N+1 문제 방지를 위한 최적화된 쿼리 사용
 */
public interface CourseDetailRetrieval {

	CourseDetailResponse getCourseDetail(Long courseId);

	Page<CourseMemberInfo> getCourseMembers(Long courseId, Pageable pageable);

	record CourseDetailResponse(CourseDetailInfo courseDetail) {
	}
}
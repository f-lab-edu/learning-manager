package me.chan99k.learningmanager.course;

/**
 * [P1] 스터디 과정 정보 수정
 */
public interface CourseInfoUpdate {
	void updateCourseInfo(Long courseId, Request request);

	/**
	 * 과정 정보 수정 요청 record
	 * 부분 업데이트를 지원하므로 모든 필드가 optional
	 * title과 description 중 하나 이상 제공
	 */
	record Request(
		String title,
		String description
	) {
	}
}

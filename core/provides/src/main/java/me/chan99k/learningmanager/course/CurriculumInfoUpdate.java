package me.chan99k.learningmanager.course;

/**
 * [P1] 커리큘럼 정보 수정
 */
public interface CurriculumInfoUpdate {
	void updateCurriculumInfo(Long requestedBy, Long courseId, Long curriculumId, Request request);

	/**
	 * 커리큘럼 정보 수정 요청 DTO
	 * 부분 업데이트를 지원하므로 모든 필드가 optional입니다.
	 * title과 description 중 하나 이상이 제공되어야 합니다.
	 */
	record Request(
		String title,
		String description
	) {
	}
}

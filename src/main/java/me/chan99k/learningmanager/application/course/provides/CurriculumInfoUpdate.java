package me.chan99k.learningmanager.application.course.provides;

import jakarta.validation.constraints.NotNull;

/**
 * [P1] 커리큘럼 정보 수정
 */
public interface CurriculumInfoUpdate {
	void updateCurriculumInfo(Long courseId, Long curriculumId, Request request);

	record Request(
		@NotNull(message = "제목을 입력해주세요")
		String title,
		String description
	) {
	}
}

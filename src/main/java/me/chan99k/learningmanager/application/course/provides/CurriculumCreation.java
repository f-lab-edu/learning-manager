package me.chan99k.learningmanager.application.course.provides;

import jakarta.validation.constraints.NotBlank;

/**
 * [P1] 스터디 커리큘럼 생성
 * <p>
 * - **주요 액터(Actor):** 사용자(스터디장)
 * <p>
 * - **사전 조건:** 스터디장은 로그인되어 있으며, 자신이 관리하는 스터디 과정이 존재한다.
 * <p>
 * - **성공 시나리오:**
 *     1. 스터디장이 커리큘럼의 제목, 설명 등 정보를 입력하고 '생성'을 요청한다.
 *     2. 시스템은 해당 과정에 새로운 `Curriculum`을 생성하여 저장한다.
 * <p>
 * - **실패 시나리오:**
 *     - **(권한 없음):** 해당 과정의 스터디장이 아닌 사용자가 생성을 시도할 경우, "권한이 없습니다."라는 메시지를 반환한다.
 * <p>
 * - **연관 도메인:** 없음
 */
public interface CurriculumCreation {
	Response createCurriculum(Long courseId, Request request);

	record Request(
		@NotBlank(message = "커리큘럼 제목은 필수입니다")
		String title,
		String description
	) {
	}

	record Response(Long curriculumId, String title) {

	}
}

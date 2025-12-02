package me.chan99k.learningmanager.course;

import jakarta.validation.constraints.NotBlank;

/**
 * [P0] 스터디 과정 생성
 * - **주요 액터(Actor):** 사용자(스터디장)
 * - **사전 조건:** 스터디장은 로그인되어 있으며, 스터디 개설 권한을 가지고 있다.
 * - **성공 시나리오:**
 *     1. 스터디장이 '스터디 과정 개설' 페이지로 이동하여 제목, 설명 등 정보를 입력하고 '생성'을 요청한다.
 *     2. 시스템은 입력 정보의 유효성을 검증하고 새로운 `Course`를 생성한다.
 *     3. 시스템은 요청한 스터디장을 해당 과정의 `MANAGER` 역할로 자동 등록한다.
 * - **실패 시나리오:**
 *     - **(권한 없음):** 스터디 개설 권한이 없는 사용자가 생성을 시도할 경우, "권한이 없습니다."라는 메시지를 반환한다.
 * - **연관 도메인:** Member
 */
public interface CourseCreation {
	Response createCourse(Request request);

	record Request(
		@NotBlank(message = "과정 명은 과정 생성에 필수입니다")
		String title,
		String description
	) {
	}

	record Response(Long courseId) {

	}
}

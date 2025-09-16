package me.chan99k.learningmanager.application.course.provides;

/**
 * [P2] 스터디 커리큘럼 삭제
 * <p>
 * - **주요 액터(Actor):** 사용자(스터디장)
 * <p>
 * - **사전 조건:** 스터디장은 로그인되어 있으며, 자신이 관리하는 스터디 과정과 삭제할 커리큘럼이 존재한다.
 * <p>
 * - **성공 시나리오:**
 *     1. 스터디장이 특정 커리큘럼의 삭제를 요청한다.
 *     2. 시스템은 해당 과정에서 `Curriculum`을 삭제한다.
 * <p>
 * - **실패 시나리오:**
 *     - **(권한 없음):** 해당 과정의 스터디장이 아닌 사용자가 삭제를 시도할 경우, "권한이 없습니다."라는 메시지를 반환한다.
 *     - **(커리큘럼 미존재):** 존재하지 않는 커리큘럼을 삭제하려 할 경우, "커리큘럼을 찾을 수 없습니다."라는 메시지를 반환한다.
 * <p>
 * - **연관 도메인:** 없음
 */
public interface CurriculumDeletion {
	void deleteCurriculum(Long courseId, Long curriculumId);

	record Request(Long curriculumId) {
	}
}

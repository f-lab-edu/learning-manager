package me.chan99k.learningmanager.session;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * [P0] 스터디 세션 생성
 * 스터디장 또는 시스템 관리자가 새로운 스터디 세션을 생성하는 기능을 제공한다.
 * - **주요 액터(Actor):** 사용자(스터디장), 시스템 관리자
 * <p>
 * - **사전 조건:**
 * <p>
 *     - (스터디장): 시스템에 로그인되어 있으며, 세션을 추가할 과정 또는 커리큘럼의 관리 권한이 있다.
 *     - (시스템 관리자): 외부 요인(예: 단발성 특강, 그룹 멘토링)에 의해 단독 세션 생성이 필요한 경우.
 * <p>
 * - **성공 시나리오:**
 *     1. 액터(스터디장 또는 시스템 관리자)가 세션의 제목, 일정, 장소 등 정보를 포함하여 생성을 요청한다.
 *     2. 요청의 종류에 따라 시스템은 다음 중 하나의 방법으로 세션을 생성한다.
 * <p>
 *         - **커리큘럼 세션 (스터디장):** 특정 커리큘럼에 속한 세션으로 생성한다.
 * <p>
 *         - **과정 세션 (스터디장):** 특정 과정에 직접 속하지만 커리큘럼에는 속하지 않는 세션으로 생성한다.
 * <p>
 *         - **단독 세션 (시스템 관리자):** 과정이나 커리큘럼에 속하지 않는 세션으로 생성한다.
 * <p>
 * - **실패 시나리오:**
 * <p>
 *     - **(세션 제약조건 위반):** 생성하려는 세션이 제약조건을 위반할 경우, 관련 오류 메시지를 반환한다.
 * <p>
 *     - **(상위 엔티티 없음):** 과정 또는 커리큘럼 세션으로 생성을 요청했으나, 지정된 상위 과정/커리큘럼이 존재하지 않을 경우 오류를 반환한다.
 * <p>
 *     - **(권한 없음):** 스터디장이 권한 없는 과정/커리큘럼에 세션 생성을 시도할 경우, "권한이 없습니다." 메시지를 반환한다.
 * <p>
 * - **연관 도메인:** Course
 */
public interface SessionCreation {
	Session createSession(Request request);

	record Request( // 과정, 커리큘럼, 세션 식별자가 모두 없으면 스탠드얼론 세션으로 처리
					@NotNull(message = "요청자 정보는 필수입니다") Long requestedBy,
					Long courseId,
					Long curriculumId,
					Long sessionId, // 특정 세션의 하위 세션일 경우
					@NotBlank(message = "세션 제목은 필수입니다") String title,
					@NotNull(message = "세션 시작 시간은 필수입니다") Instant scheduledAt,
					@NotNull(message = "세션 종료 시간은 필수입니다") Instant scheduledEndAt,
					@NotNull(message = "세션 타입은 필수입니다") SessionType type,
					@NotNull(message = "세션 장소는 필수입니다") SessionLocation location,
					String locationDetails
	) {

	}

	record Response(
		Long id,
		String title,
		Instant scheduledAt,
		Instant scheduledEndAt,
		SessionType type,
		SessionLocation location,
		String locationDetails,
		Long courseId,
		Long curriculumId
	) {
	}
}

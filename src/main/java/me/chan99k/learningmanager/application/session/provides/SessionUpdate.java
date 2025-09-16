package me.chan99k.learningmanager.application.session.provides;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

/**
 * [P1] 스터디 세션 정보 수정
 * <p>
 * - **주요 액터(Actor):** 사용자(스터디장), 시스템 관리자
 * <p>
 * - **사전 조건:** 
 *     - (스터디장): 로그인되어 있으며, 수정할 세션의 과정 관리 권한이 있다.
 *     - (시스템 관리자): 단독 세션의 경우 시스템 관리자 권한이 있다.
 *     - 수정할 세션이 존재하고, 수정 가능한 상태(시작 전)여야 한다.
 * <p>
 * - **성공 시나리오:**
 *     1. 액터가 세션의 제목, 일정, 타입, 장소 등 정보를 수정하여 요청한다.
 *     2. 시스템은 수정 권한을 확인한다.
 *     3. 시스템은 세션 정보를 업데이트한다.
 * <p>
 * - **실패 시나리오:**
 *     - **(권한 없음):** 해당 세션을 수정할 권한이 없는 사용자가 시도할 경우, "권한이 없습니다."라는 메시지를 반환한다.
 *     - **(세션 미존재):** 존재하지 않는 세션을 수정하려 할 경우, "세션을 찾을 수 없습니다."라는 메시지를 반환한다.
 *     - **(수정 불가):** 이미 시작된 세션이나 수정 기한이 지난 세션의 경우, "수정할 수 없는 세션입니다."라는 메시지를 반환한다.
 *     - **(제약조건 위반):** 수정하려는 정보가 제약조건을 위반할 경우, 관련 오류 메시지를 반환한다.
 * <p>
 * - **연관 도메인:** Course
 */
public interface SessionUpdate {
	void updateSession(Long sessionId, Request request);

	record Request(
		@NotBlank(message = "세션 제목은 필수입니다")
		String title,
		@NotNull(message = "세션 시작 시간은 필수입니다")
		Instant scheduledAt,
		@NotNull(message = "세션 종료 시간은 필수입니다")
		Instant scheduledEndAt,
		@NotNull(message = "세션 타입은 필수입니다")
		SessionType type,
		@NotNull(message = "세션 장소는 필수입니다")
		SessionLocation location,
		String locationDetails
	) {
	}
}

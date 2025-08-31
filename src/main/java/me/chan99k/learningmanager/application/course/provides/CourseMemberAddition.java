package me.chan99k.learningmanager.application.course.provides;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.chan99k.learningmanager.domain.course.CourseRole;

/**
 * [P0] 스터디 멤버 추가
 * <p>
 - **주요 액터(Actor):** 사용자(스터디장)
 * <p>
 - **사전 조건:** 스터디장은 로그인되어 있으며, 멤버를 추가할 과정을 관리하고 있다.
 * <p>
 - **성공 시나리오:**
 * <p>
 1. 스터디장이 멤버 관리 페이지에서 '멤버 추가'를 요청한다.
 2. 스터디장이 추가할 회원의 정보(예: 이메일 또는 닉네임)와 역할을 입력한다.
 3. 시스템은 해당 회원이 존재하는지, 그리고 이미 과정에 참여 중인지 확인한다.
 4. 시스템은 해당 회원을 `CourseMember`로 등록하고 지정된 역할을 부여한다.
 * <p>
 - **실패 시나리오:**
 * <p>
 - **(권한 없음):** 해당 과정의 스터디장이 아닌 사용자가 멤버 추가를 시도할 경우, "권한이 없습니다."라는 메시지를 반환한다.
 * <p>
 - **(이미 참여 중):** 추가하려는 회원이 이미 과정에 참여 중일 경우, "이미 참여 중인 회원입니다."라는 메시지를 반환한다.
 * <p>
 - **(존재하지 않는 회원):** 추가하려는 회원이 시스템에 존재하지 않을 경우, "존재하지 않는 회원입니다."라는 메시지를 반환한다.
 * <p>
 - **연관 도메인:** Member, Notification

 */
public interface CourseMemberAddition {
	Response addMemberToCourse(Long courseId, Request request);

	record Request(@NotBlank @Email String email, @NotNull CourseRole role) {

	}

	record Response() {

	}
}

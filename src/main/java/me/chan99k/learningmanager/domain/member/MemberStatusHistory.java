package me.chan99k.learningmanager.domain.member;

import java.time.Instant;

// TODO :     3. 시스템은 회원의 상태를 변경하고, `MemberStatusHistory`에 이력을 기록한다. --> 로직 누락, 상태 변경 이력 테이블 추가 및 MemberService 에서 서비스 계층에서 처리하도록 하기
public record MemberStatusHistory(
	Long id,
	Long memberId,
	MemberStatus status,
	String reason,
	Instant changedAt
) {
}

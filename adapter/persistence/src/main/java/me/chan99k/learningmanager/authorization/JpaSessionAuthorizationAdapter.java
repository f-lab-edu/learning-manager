package me.chan99k.learningmanager.authorization;

import java.util.List;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.course.CourseRole;

@Repository
public class JpaSessionAuthorizationAdapter implements SessionAuthorizationPort {

	@Override
	public boolean hasRoleForSession(Long memberId, Long sessionId, CourseRole role) {
		// TODO: 세션 ID로 과정 ID를 조회한 후 권한 확인
		return false;
	}

	@Override
	public boolean hasAnyRoleForSession(Long memberId, Long sessionId, List<CourseRole> roles) {
		// TODO: 세션 ID로 과정 ID를 조회한 후 권한 확인
		return false;
	}

	@Override
	public boolean isMemberOfSession(Long memberId, Long sessionId) {
		// TODO: 세션 ID로 과정 ID를 조회한 후 멤버 여부 확인
		return false;
	}
}

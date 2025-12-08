package me.chan99k.learningmanager.authorization;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.course.CourseRole;

@Service("memberSecurity")
public class MemberSecurityService {

	public boolean isOwner(Long targetMemberId, Long currentMemberId) {
		if (targetMemberId == null || currentMemberId == null) {
			return false;
		}
		return targetMemberId.equals(currentMemberId);
	}

	public boolean isNotOwner(Long targetMemberId, Long currentMemberId) {
		return !isOwner(targetMemberId, currentMemberId);
	}

	/**
	 * 본인이거나 시스템 관리자인 경우
	 */
	public boolean isOwnerOrSystemAdmin(Long targetMemberId, Long currentMemberId, boolean isAdmin) {
		return isOwner(targetMemberId, currentMemberId) || isAdmin;
	}

	/**
	 * 본인이거나 해당 과정의 매니저인 경우
	 * (과정 내에서 다른 멤버 정보 조회 등)
	 */
	public boolean isOwnerOrCourseManager(
		Long targetMemberId,
		Long currentMemberId,
		Long courseId,
		CourseAuthorizationPort courseAuthPort
	) {
		return isOwner(targetMemberId, currentMemberId)
			|| courseAuthPort.hasRole(currentMemberId, courseId, CourseRole.MANAGER);
	}
}

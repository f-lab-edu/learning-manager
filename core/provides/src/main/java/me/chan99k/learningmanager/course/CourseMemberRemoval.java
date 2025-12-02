package me.chan99k.learningmanager.course;

/**
 * [P2] 스터디 과정 멤버 제외
 */
public interface CourseMemberRemoval {
	void removeMemberFromCourse(Long courseId, Long memberId);
}

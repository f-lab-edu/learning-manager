package me.chan99k.learningmanager.authorization;

public interface CourseSecurity {

	boolean isManager(Long courseId, Long memberId);

	boolean isManagerOrMentor(Long courseId, Long memberId);

	boolean isMember(Long courseId, Long memberId);
}

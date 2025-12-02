package me.chan99k.learningmanager.course;

public enum CourseRole {
	MANAGER("과정 매니저"), MENTOR("과정 멘토"), MENTEE("과정 멘티");

	public final String value;

	CourseRole(String value) {
		this.value = value;
	}
}

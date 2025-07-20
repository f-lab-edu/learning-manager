package me.chan99k.learningmanager.domain.study;

public enum SessionLocation {
	GOOGLE_MEET("구글 밋"), ZOOM("줌"), SITE("사용자 지정 장소");

	public final String location;

	SessionLocation(String location) {
		this.location = location;
	}
}

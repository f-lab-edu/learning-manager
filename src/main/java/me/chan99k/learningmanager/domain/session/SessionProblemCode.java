package me.chan99k.learningmanager.domain.session;

import me.chan99k.learningmanager.common.exception.ProblemCode;

public enum SessionProblemCode implements ProblemCode {
	COURSE_ID_REQUIRED("DLS001", "[System] 코스 ID는 필수입니다."),
	CURRICULUM_ID_REQUIRED("DLS002", "[System] 커리큘럼 ID는 필수입니다."),
	INVALID_SESSION_HIERARCHY("DLS003", "[System] 하위 세션은 또 다른 하위 세션을 가질 수 없습니다."),
	ALREADY_PARTICIPATING_MEMBER("DLS004", "[System] 이미 세션에 참여 중인 멤버입니다."),
	MEMBER_NOT_PARTICIPATING("DLS005", "[System] 해당 세션에 참여하지 않는 멤버입니다."),
	ONLY_ONE_HOST_ALLOWED("DLS006", "[System] 한 세션에는 한 명의 호스트만 지정할 수 있습니다."),
	CANNOT_MODIFY_STARTED_SESSION("DLS007", "[System] 이미 시작된 세션은 수정할 수 없습니다."),
	ROOT_SESSION_MODIFICATION_DEADLINE_EXCEEDED("DLS008", "[System] 루트 세션은 시작 3일 전까지만 수정할 수 있습니다."),
	CHILD_SESSION_MODIFICATION_DEADLINE_EXCEEDED("DLS009", "[System] 하위 세션은 시작 1시간 전까지만 수정할 수 있습니다."),
	SESSION_START_TIME_REQUIRED("DLS010", "[System] 세션 시작 시간은 필수입니다."),
	SESSION_END_TIME_REQUIRED("DLS011", "[System] 세션 종료 시간은 필수입니다."),
	START_TIME_MUST_BE_BEFORE_END_TIME("DLS012", "[System] 세션 시작 시간은 종료 시간보다 빨라야 합니다."),
	SESSION_DURATION_EXCEEDS_24_HOURS("DLS013", "[System] 세션은 24시간을 초과할 수 없습니다."),
	SESSION_CANNOT_SPAN_MULTIPLE_DAYS("DLS014", "[System] 세션은 이틀에 걸쳐 진행될 수 없습니다."),
	OFFLINE_SESSION_LOCATION_DETAIL_REQUIRED("DLS015", "[System] 오프라인 세션은 상세 장소 설명이 필수입니다."),
	ONLINE_SESSION_CANNOT_HAVE_LOCATION_DETAIL("DLS016", "[System] 온라인 세션은 상세 장소 설명을 가질 수 없습니다."),
	CHILD_SESSION_MUST_HAVE_PARENT("DLS017", "[System] 하위 세션은 반드시 부모 세션을 가져야 합니다."),
	CHILD_SESSION_START_TIME_BEFORE_PARENT("DLS018", "[System] 하위 세션의 시작 시간은 부모 세션의 시작 시간보다 빠를 수 없습니다."),
	CHILD_SESSION_END_TIME_AFTER_PARENT("DLS019", "[System] 하위 세션의 종료 시간은 부모 세션의 종료 시간보다 늦을 수 없습니다."),
	SAME_ROLE_PARTICIPANT_ALREADY("DLS020", "[System] 이미 해당 역할을 가지고 있습니다."),
	SESSION_REQUIRED("DLS021", "[System] 세션 참여를 위해서는 유효한 세션이 필요합니다."),
	MEMBER_ID_REQUIRED("DLS022", "[System] 세션 참여를 위해서는 유효한 회원 ID가 필요합니다."),
	PARTICIPANT_ROLE_REQUIRED("DLS023", "[System] 세션 참여를 위해서는 유효한 역할이 필요합니다."),
	SESSION_NOT_FOUND("DLS024", "[System] 세션을 찾을 수 없습니다.");

	private final String code;
	private final String message;

	SessionProblemCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
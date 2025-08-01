package me.chan99k.learningmanager.domain.course;

import me.chan99k.learningmanager.common.exception.ProblemCode;

public enum CourseProblemCode implements ProblemCode {
	// Course related
	COURSE_TITLE_REQUIRED("DCL001", "[System] 과정명은 필수값 입니다."),
	COURSE_DESCRIPTION_REQUIRED("DCL002", "[System] 과정에 대한 설명 값이 비어 있습니다."),
	COURSE_MEMBER_ALREADY_REGISTERED("DCL003", "[System] 이미 과정에 등록된 멤버입니다."),
	COURSE_MEMBER_NOT_REGISTERED("DCL004", "[System] 과정에 등록되지 않은 멤버입니다."),
	CURRICULUM_NULL("DCL005", "[System] 제거할 커리큘럼은 null일 수 없습니다."),
	CURRICULUM_NOT_FOUND_IN_COURSE("DCL006", "[System] 해당 과정에 존재하지 않는 커리큘럼입니다."),

	// CourseMember related
	COURSE_REQUIRED("DCL007", "[System] 코스는 필수입니다."),
	MEMBER_ID_REQUIRED("DCL008", "[System] 멤버 ID는 필수입니다."),
	COURSE_ROLE_REQUIRED("DCL009", "[System] 코스 역할은 필수입니다."),
	NEW_ROLE_REQUIRED("DCL010", "[System] 새로운 역할은 필수입니다."),

	// Curriculum related
	CURRICULUM_COURSE_REQUIRED("DCL011", "[System] 커리큘럼은 반드시 코스에 속해야 합니다."),
	CURRICULUM_TITLE_REQUIRED("DCL012", "[System] 커리큘럼명은 필수입니다."),
	CURRICULUM_DESCRIPTION_REQUIRED("DCL013", "[System] 커리큘럼에 대한 설명 값이 비어 있습니다.");

	private final String code;
	private final String message;

	CourseProblemCode(String code, String message) {
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

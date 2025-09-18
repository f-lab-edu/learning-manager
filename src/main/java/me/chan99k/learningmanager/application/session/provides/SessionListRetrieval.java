package me.chan99k.learningmanager.application.session.provides;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

/**
 * 세션 목록 조회
 */
public interface SessionListRetrieval {

	Page<SessionListResponse> getSessionList(SessionListRequest request);

	Page<SessionListResponse> getCourseSessionList(Long courseId, CourseSessionListRequest request);

	Page<SessionListResponse> getCurriculumSessionList(Long curriculumId, CurriculumSessionListRequest request);

	Page<SessionListResponse> getUserSessionList(Long memberId, UserSessionListRequest request);

	Map<LocalDate, List<SessionCalendarResponse>> getSessionCalendar(YearMonth yearMonth,
		SessionCalendarRequest request);

	enum SessionStatus { // 계산된 값 -> UI 표시를 원활히 하기 위해서
		UPCOMING,    // 예정
		ONGOING,     // 진행 중
		COMPLETED    // 완료
	}

	record SessionListRequest(
		int page,
		int size,
		String sort,
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate
	) {
		public SessionListRequest {
			if (page < 0) {
				page = 0;
			}
			if (size <= 0 || size > 100) {
				size = 20;
			}
			if (sort == null) {
				sort = "scheduledAt,desc";
			}
		}
	}

	record CourseSessionListRequest(
		int page,
		int size,
		String sort,
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate,
		Boolean includeChildSessions
	) {
		public CourseSessionListRequest {
			if (page < 0) {
				page = 0;
			}
			if (size <= 0 || size > 100) {
				size = 20;
			}
			if (sort == null) {
				sort = "scheduledAt,desc";
			}
			if (includeChildSessions == null) {
				includeChildSessions = true;
			}
		}
	}

	record CurriculumSessionListRequest(
		int page,
		int size,
		String sort,
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate,
		Boolean includeChildSessions
	) {
		public CurriculumSessionListRequest {
			if (page < 0) {
				page = 0;
			}
			if (size <= 0 || size > 100) {
				size = 20;
			}
			if (sort == null) {
				sort = "scheduledAt,desc";
			}
			if (includeChildSessions == null)
				includeChildSessions = true;
		}
	}

	record UserSessionListRequest(
		int page,
		int size,
		String sort,
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate
	) {
		public UserSessionListRequest {
			if (page < 0) {
				page = 0;
			}
			if (size <= 0 || size > 100) {
				size = 20;
			}
			if (sort == null) {
				sort = "scheduledAt,desc";
			}
		}
	}

	record SessionCalendarRequest(
		SessionType type,
		SessionLocation location,
		Long courseId,
		Long curriculumId
	) {
	}

	record SessionListResponse(
		Long id,
		String title,
		Instant scheduledAt,
		Instant scheduledEndAt,
		SessionType type,
		SessionLocation location,
		String locationDetails,
		Long courseId,
		Long curriculumId,
		Long parentId,
		int childSessionCount,
		int participantCount,
		SessionStatus status
	) {
	}

	record SessionCalendarResponse(
		Long id,
		String title,
		Instant scheduledAt,
		Instant scheduledEndAt,
		SessionType type,
		SessionLocation location,
		String locationDetails,
		Long courseId,
		Long curriculumId,
		SessionStatus status
	) {
	}
}
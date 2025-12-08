package me.chan99k.learningmanager.attendance;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.course.CourseMemberInfo;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.session.SessionQueryRepository;
import me.chan99k.learningmanager.session.dto.SessionInfo;

@Service
@Transactional(readOnly = true)
public class CourseAttendanceRetrievalService implements CourseAttendanceRetrieval {

	private final AttendanceQueryRepository attendanceQueryRepository;
	private final SessionQueryRepository sessionQueryRepository;
	private final CourseQueryRepository courseQueryRepository;

	public CourseAttendanceRetrievalService(AttendanceQueryRepository attendanceQueryRepository,
		SessionQueryRepository sessionQueryRepository, CourseQueryRepository courseQueryRepository) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.sessionQueryRepository = sessionQueryRepository;
		this.courseQueryRepository = courseQueryRepository;
	}

	@Override
	public Response getAllMembersAttendance(Long requestedBy, AllMembersRequest request) {
		List<Long> sessionIds = findSessionIds(request);
		if (sessionIds.isEmpty()) {
			return emptyResponse();
		}

		List<CourseMemberInfo> courseMembers = findCourseMembers(request.courseId());
		List<Long> memberIds = courseMembers.stream()
			.map(CourseMemberInfo::memberId)
			.toList();

		if (memberIds.isEmpty()) {
			return emptyResponse();
		}

		List<AttendanceQueryRepository.MemberAttendanceResult> results =
			attendanceQueryRepository.findAllMembersAttendanceWithStats(sessionIds, memberIds);

		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(sessionIds);

		Map<Long, String> memberNameMap = courseMembers.stream()
			.collect(Collectors.toMap(
				CourseMemberInfo::memberId,
				CourseMemberInfo::nickname,
				(existing, replacement) -> existing
			));

		List<MemberAttendanceSummary> summaries = results.stream()
			.map(result -> buildMemberSummary(result, memberNameMap, sessionInfoMap))
			.toList();

		return new Response(summaries, calculateCourseStatistics(summaries));
	}

	@Override
	public Response getMemberAttendance(Long requestedBy, MemberRequest request) {
		List<Long> sessionIds = findSessionIds(request);
		if (sessionIds.isEmpty()) {
			return emptyResponse();
		}

		AttendanceQueryRepository.MemberAttendanceResult result =
			attendanceQueryRepository.findMemberAttendanceWithStats(request.memberId(), sessionIds);

		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(sessionIds);

		String memberName = findMemberName(request.courseId(), request.memberId());

		Map<Long, String> memberNameMap = Map.of(request.memberId(), memberName);

		MemberAttendanceSummary summary = buildMemberSummary(result, memberNameMap, sessionInfoMap);

		return new Response(List.of(summary), calculateCourseStatistics(List.of(summary)));
	}

	private List<Long> findSessionIds(AllMembersRequest request) {
		if (request.startDate() != null && request.endDate() != null) {
			return sessionQueryRepository.findSessionIdsByPeriodAndFilters(
				request.startDate(), request.endDate(),
				request.courseId(), request.curriculumId()
			);
		}
		if (request.year() != null && request.month() != null) {
			return sessionQueryRepository.findSessionIdsByMonthAndFilters(
				request.year(), request.month(),
				request.courseId(), request.curriculumId()
			);
		}
		if (request.curriculumId() != null) {
			return sessionQueryRepository.findSessionIdsByCurriculumId(request.curriculumId());
		}
		return sessionQueryRepository.findSessionIdsByCourseId(request.courseId());
	}

	private List<Long> findSessionIds(MemberRequest request) {
		if (request.startDate() != null && request.endDate() != null) {
			return sessionQueryRepository.findSessionIdsByPeriodAndFilters(
				request.startDate(), request.endDate(),
				request.courseId(), request.curriculumId()
			);
		}
		if (request.year() != null && request.month() != null) {
			return sessionQueryRepository.findSessionIdsByMonthAndFilters(
				request.year(), request.month(),
				request.courseId(), request.curriculumId()
			);
		}
		if (request.curriculumId() != null) {
			return sessionQueryRepository.findSessionIdsByCurriculumId(request.curriculumId());
		}
		return sessionQueryRepository.findSessionIdsByCourseId(request.courseId());
	}

	private List<CourseMemberInfo> findCourseMembers(Long courseId) {
		return courseQueryRepository.findCourseMembersByCourseId(
			courseId,
			PageRequest.of(0, Integer.MAX_VALUE)
		).content();
	}

	private String findMemberName(Long courseId, Long memberId) {
		return findCourseMembers(courseId).stream()
			.filter(m -> m.memberId().equals(memberId))
			.findFirst()
			.map(CourseMemberInfo::nickname)
			.orElse("Unknown");
	}

	private MemberAttendanceSummary buildMemberSummary(
		AttendanceQueryRepository.MemberAttendanceResult result,
		Map<Long, String> memberNameMap,
		Map<Long, SessionInfo> sessionInfoMap
	) {
		List<SessionAttendanceInfo> sessions = result.attendances().stream()
			.map(a -> {
				SessionInfo info = sessionInfoMap.get(a.sessionId());
				return new SessionAttendanceInfo(
					a.attendanceId(),
					a.sessionId(),
					info != null ? info.sessionTitle() : "Unknown",
					info != null ? info.scheduledAt() : null,
					a.finalStatus(),
					info != null ? info.curriculumId() : null,
					info != null ? info.curriculumTitle() : "Unknown"
				);
			})
			.toList();

		MemberStatistics stats = new MemberStatistics(
			result.stats().total(),
			result.stats().present(),
			result.stats().absent(),
			result.stats().late(),
			result.stats().leftEarly(),
			result.stats().rate()
		);

		return new MemberAttendanceSummary(
			result.memberId(),
			memberNameMap.getOrDefault(result.memberId(), "Unknown"),
			sessions,
			stats
		);
	}

	private CourseAttendanceStatistics calculateCourseStatistics(List<MemberAttendanceSummary> summaries) {
		if (summaries.isEmpty()) {
			return new CourseAttendanceStatistics(0, 0, 0.0);
		}

		int totalMembers = summaries.size();
		int totalSessions = summaries.get(0).sessions().size();
		double avgRate = summaries.stream()
			.mapToDouble(s -> s.statistics().attendanceRate())
			.average()
			.orElse(0.0);

		return new CourseAttendanceStatistics(totalMembers, totalSessions, avgRate);
	}

	private Response emptyResponse() {
		return new Response(List.of(), new CourseAttendanceStatistics(0, 0, 0.0));
	}
}

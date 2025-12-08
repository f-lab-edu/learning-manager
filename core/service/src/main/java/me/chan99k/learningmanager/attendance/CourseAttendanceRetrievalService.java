package me.chan99k.learningmanager.attendance;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		List<Long> sessionIds = findSessionIds(SessionFilter.from(request));
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
		List<Long> sessionIds = findSessionIds(SessionFilter.from(request));
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

	// 헬퍼 메서드

	private List<Long> findSessionIds(SessionFilter filter) {
		if (filter.startDate() != null && filter.endDate() != null) {
			return sessionQueryRepository.findSessionIdsByPeriodAndFilters(
				filter.startDate(), filter.endDate(),
				filter.courseId(), filter.curriculumId()
			);
		}
		if (filter.year() != null && filter.month() != null) {
			return sessionQueryRepository.findSessionIdsByMonthAndFilters(
				filter.year(), filter.month(),
				filter.courseId(), filter.curriculumId()
			);
		}
		if (filter.curriculumId() != null) {
			return sessionQueryRepository.findSessionIdsByCurriculumId(filter.curriculumId());
		}
		return sessionQueryRepository.findSessionIdsByCourseId(filter.courseId());
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
			.map(a -> toSessionAttendanceInfo(a, sessionInfoMap))
			.toList();

		return new MemberAttendanceSummary(
			result.memberId(),
			memberNameMap.getOrDefault(result.memberId(), "Unknown"),
			sessions,
			toMemberStatistics(result.stats())
		);
	}

	private SessionAttendanceInfo toSessionAttendanceInfo(
		AttendanceQueryRepository.AttendanceRecord record,
		Map<Long, SessionInfo> sessionInfoMap
	) {
		Optional<SessionInfo> infoOpt = Optional.ofNullable(
			sessionInfoMap.get(record.sessionId())
		);

		return new SessionAttendanceInfo(
			record.attendanceId(),
			record.sessionId(),
			infoOpt.map(SessionInfo::sessionTitle).orElse("Unknown"),
			infoOpt.map(SessionInfo::scheduledAt).orElse(null),
			record.finalStatus(),
			infoOpt.map(SessionInfo::curriculumId).orElse(null),
			infoOpt.map(SessionInfo::curriculumTitle).orElse("Unknown")
		);
	}

	private MemberStatistics toMemberStatistics(AttendanceQueryRepository.AttendanceStats stats) {
		return new MemberStatistics(
			stats.total(),
			stats.present(),
			stats.absent(),
			stats.late(),
			stats.leftEarly(),
			stats.rate()
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

	private record SessionFilter(
		Long courseId,
		Long curriculumId,
		Integer year,
		Integer month,
		Instant startDate,
		Instant endDate
	) {
		static SessionFilter from(AllMembersRequest request) {
			return new SessionFilter(
				request.courseId(), request.curriculumId(),
				request.year(), request.month(),
				request.startDate(), request.endDate()
			);
		}

		static SessionFilter from(MemberRequest request) {
			return new SessionFilter(
				request.courseId(), request.curriculumId(),
				request.year(), request.month(),
				request.startDate(), request.endDate()
			);
		}
	}
}

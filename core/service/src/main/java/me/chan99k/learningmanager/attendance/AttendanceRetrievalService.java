package me.chan99k.learningmanager.attendance;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.session.SessionQueryRepository;
import me.chan99k.learningmanager.session.dto.SessionInfo;

@Service
public class AttendanceRetrievalService implements AttendanceRetrieval {

	private final AttendanceQueryRepository attendanceQueryRepository;
	private final SessionQueryRepository sessionQueryRepository;

	public AttendanceRetrievalService(AttendanceQueryRepository attendanceQueryRepository,
		SessionQueryRepository sessionQueryRepository) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.sessionQueryRepository = sessionQueryRepository;
	}

	@Override
	public Response getMyAllAttendanceStatus(AllAttendanceRequest request) {
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByMemberId(request.memberId());

		return buildResponse(request.memberId(), sessionIds);
	}

	@Override
	public Response getMyCourseAttendanceStatus(CourseAttendanceRequest request) {
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByCourseId(request.courseId());

		return buildResponse(request.memberId(), sessionIds);
	}

	@Override
	public Response getMyCurriculumAttendanceStatus(CurriculumAttendanceRequest request) {
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByCurriculumId(request.curriculumId());

		return buildResponse(request.memberId(), sessionIds);
	}

	@Override
	public Response getMyMonthlyAttendanceStatus(MonthlyAttendanceRequest request) {
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByMonthAndFilters(
			request.year(), request.month(), request.courseId(), request.curriculumId()
		);

		return buildResponse(request.memberId(), sessionIds);
	}

	@Override
	public Response getMyPeriodAttendanceStatus(PeriodAttendanceRequest request) {
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByPeriodAndFilters(
			request.startDate(), request.endDate(), request.courseId(), request.curriculumId()
		);

		var result = attendanceQueryRepository.findMemberAttendanceWithStats(
			request.memberId(), sessionIds
		);

		// 상태 필터링 (request.status가 있는 경우)
		List<AttendanceQueryRepository.AttendanceRecord> filteredAttendances =
			request.status() != null
				? result.attendances().stream()
				.filter(a -> a.finalStatus() == request.status())
				.toList()
				: result.attendances();

		List<Long> attendanceSessionIds = filteredAttendances.stream()
			.map(AttendanceQueryRepository.AttendanceRecord::sessionId)
			.toList();

		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(attendanceSessionIds);

		List<SessionAttendanceInfo> sessionInfos =
			buildSessionAttendanceInfos(filteredAttendances, sessionInfoMap);

		// 필터링 후 통계 재계산 필요
		AttendanceStatistics statistics = AttendanceStatistics.calculate(sessionInfos);

		return new Response(sessionInfos, statistics);
	}

	private Response buildResponse(Long memberId, List<Long> sessionIds) {
		if (sessionIds.isEmpty()) {
			return new Response(List.of(), emptyStatistics());
		}

		var result = attendanceQueryRepository.findMemberAttendanceWithStats(memberId, sessionIds);

		List<Long> attendanceSessionIds = result.attendances().stream()
			.map(AttendanceQueryRepository.AttendanceRecord::sessionId)
			.toList();

		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(attendanceSessionIds);

		// 결과 조합
		List<SessionAttendanceInfo> sessionInfos =
			buildSessionAttendanceInfos(result.attendances(), sessionInfoMap);

		AttendanceStatistics statistics = mapToStatistics(result.stats());

		return new Response(sessionInfos, statistics);
	}

	private List<SessionAttendanceInfo> buildSessionAttendanceInfos(
		List<AttendanceQueryRepository.AttendanceRecord> attendances,
		Map<Long, SessionInfo> sessionInfoMap
	) {
		return attendances.stream()
			.map(attendance -> {
				SessionInfo sessionInfo = sessionInfoMap.get(attendance.sessionId());

				if (sessionInfo == null) {
					return new SessionAttendanceInfo(
						attendance.attendanceId(),
						attendance.sessionId(),
						"Unknown Session",
						null,
						attendance.finalStatus(),
						null, "Unknown Course",
						null, "Unknown Curriculum"
					);
				}

				return new SessionAttendanceInfo(
					attendance.attendanceId(),
					attendance.sessionId(),
					sessionInfo.sessionTitle(),
					sessionInfo.scheduledAt(),
					attendance.finalStatus(),
					sessionInfo.courseId(),
					sessionInfo.courseTitle(),
					sessionInfo.curriculumId(),
					sessionInfo.curriculumTitle()
				);
			})
			.toList();
	}

	private AttendanceStatistics mapToStatistics(AttendanceQueryRepository.AttendanceStats stats) {
		return new AttendanceStatistics(
			stats.total(),
			stats.present(),
			stats.absent(),
			stats.late(),
			stats.leftEarly(),
			stats.rate()
		);
	}

	private AttendanceStatistics emptyStatistics() {
		return new AttendanceStatistics(0, 0, 0, 0, 0, 0.0);
	}
}

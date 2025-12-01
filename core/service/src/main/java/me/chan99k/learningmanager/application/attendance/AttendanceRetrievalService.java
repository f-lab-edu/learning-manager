package me.chan99k.learningmanager.application.attendance;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.application.attendance.provides.AttendanceRetrieval;
import me.chan99k.learningmanager.application.session.SessionQueryRepository;
import me.chan99k.learningmanager.application.session.dto.SessionInfo;

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
		// 해당 회원의 모든 출석 기록 조회
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByMemberId(request.memberId());
		List<AttendanceQueryRepository.AttendanceProjection> attendances =
			attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(
				request.memberId(),
				sessionIds
			);

		// 세션 정보 조회
		List<Long> attendanceSessionIds = attendances.stream()
			.map(AttendanceQueryRepository.AttendanceProjection::sessionId)
			.toList();

		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(attendanceSessionIds);

		// 결과 조합
		List<SessionAttendanceInfo> sessionInfos = buildSessionAttendanceInfos(attendances, sessionInfoMap);
		AttendanceStatistics statistics = AttendanceStatistics.calculate(sessionInfos);

		return new Response(sessionInfos, statistics);

	}

	@Override
	public Response getMyCourseAttendanceStatus(CourseAttendanceRequest request) {
		// 해당 과정의 모든 세션 ID 조회
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByCourseId(request.courseId());

		// 해당 세션들에 대한 출석 기록 조회
		List<AttendanceQueryRepository.AttendanceProjection> attendances =
			attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(
				request.memberId(), sessionIds
			);

		// 세션 정보 조회
		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(sessionIds);

		// 결과 조합
		List<SessionAttendanceInfo> sessionInfos = buildSessionAttendanceInfos(attendances, sessionInfoMap);
		AttendanceStatistics statistics = AttendanceStatistics.calculate(sessionInfos);

		return new Response(sessionInfos, statistics);

	}

	@Override
	public Response getMyCurriculumAttendanceStatus(CurriculumAttendanceRequest request) {
		// 해당 커리큘럼의 모든 세션 ID 조회
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByCurriculumId(request.curriculumId());

		// 해당 세션들에 대한 출석 기록 조회
		List<AttendanceQueryRepository.AttendanceProjection> attendances =
			attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(
				request.memberId(), sessionIds
			);

		// 세션 정보 조회
		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(sessionIds);

		// 결과 조합
		List<SessionAttendanceInfo> sessionInfos = buildSessionAttendanceInfos(attendances, sessionInfoMap);
		AttendanceStatistics statistics = AttendanceStatistics.calculate(sessionInfos);

		return new Response(sessionInfos, statistics);

	}

	@Override
	public Response getMyMonthlyAttendanceStatus(MonthlyAttendanceRequest request) {
		// 해당 월의 세션 ID 조회
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByMonthAndFilters(
			request.year(), request.month(), request.courseId(), request.curriculumId()
		);

		// 해당 월의 세션들에 대한 출석 기록 조회
		List<AttendanceQueryRepository.AttendanceProjection> attendances =
			attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(
				request.memberId(), sessionIds
			);

		// 세션 정보 조회
		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(sessionIds);

		// 결과 조합
		List<SessionAttendanceInfo> sessionInfos = buildSessionAttendanceInfos(attendances, sessionInfoMap);
		AttendanceStatistics statistics = AttendanceStatistics.calculate(sessionInfos);

		return new Response(sessionInfos, statistics);

	}

	@Override
	public Response getMyPeriodAttendanceStatus(PeriodAttendanceRequest request) {
		// 해당 기간의 세션 ID 조회 (필터링 포함)
		List<Long> sessionIds = sessionQueryRepository.findSessionIdsByPeriodAndFilters(
			request.startDate(), request.endDate(), request.courseId(), request.curriculumId()
		);

		// 해당 세션들에 대한 출석 기록 조회
		List<AttendanceQueryRepository.AttendanceProjection> attendances =
			attendanceQueryRepository.findAttendanceProjectionByMemberIdAndSessionIds(
				request.memberId(), sessionIds
			);

		// 상태 필터링 적용
		if (request.status() != null) {
			attendances = attendances.stream()
				.filter(a -> a.finalStatus() == request.status())
				.toList();
		}

		// 세션 정보 조회
		Map<Long, SessionInfo> sessionInfoMap =
			sessionQueryRepository.findSessionInfoMapByIds(sessionIds);

		// 결과 조합
		List<SessionAttendanceInfo> sessionInfos = buildSessionAttendanceInfos(attendances, sessionInfoMap);
		AttendanceStatistics statistics = AttendanceStatistics.calculate(sessionInfos);

		return new Response(sessionInfos, statistics);

	}

	private List<SessionAttendanceInfo> buildSessionAttendanceInfos(
		List<AttendanceQueryRepository.AttendanceProjection> attendances,
		Map<Long, SessionInfo> sessionInfoMap
	) {
		return attendances.stream()
			.map(attendance -> {
				SessionInfo sessionInfo = sessionInfoMap.get(attendance.sessionId());
				if (sessionInfo == null) {
					// 세션 정보가 없는 경우 로그 남기고 기본값 처리
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

}

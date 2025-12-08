package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;
import me.chan99k.learningmanager.attendance.Attendance;
import me.chan99k.learningmanager.attendance.AttendanceQueryRepository;
import me.chan99k.learningmanager.attendance.AttendanceStatus;

@Repository
public class AttendanceQueryAdapter implements AttendanceQueryRepository {

	private final AttendanceMongoRepository repository;

	public AttendanceQueryAdapter(AttendanceMongoRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Attendance> findBySessionIdAndMemberId(Long sessionId, Long memberId) {
		return repository
			.findBySessionIdAndMemberId(sessionId, memberId)
			.map(AttendanceDocument::toDomain);
	}

	@Override
	public MemberAttendanceResult findMemberAttendanceWithStats(Long memberId, List<Long> sessionIds) {
		if (sessionIds.isEmpty()) {
			return emptyResult(memberId);
		}

		var result = repository.aggregateMemberAttendance(memberId, sessionIds);

		return result != null ? mapToResult(result) : emptyResult(memberId);
	}

	@Override
	public List<MemberAttendanceResult> findAllMembersAttendanceWithStats(List<Long> sessionIds, List<Long> memberIds) {
		if (sessionIds.isEmpty() || memberIds.isEmpty()) {
			return List.of();
		}

		return repository.aggregateAllMembersAttendance(sessionIds, memberIds)
			.stream()
			.map(this::mapToResult)
			.toList();
	}

	private MemberAttendanceResult emptyResult(Long memberId) {
		return new MemberAttendanceResult(
			memberId,
			List.of(),
			new AttendanceStats(0, 0, 0, 0, 0, 0.0)
		);
	}

	private MemberAttendanceResult mapToResult(AttendanceMongoRepository.MemberAttendanceAggregationInfo document) {
		List<AttendanceRecord> records = document.attendances().stream()
			.map(att -> new AttendanceRecord(
				att.attendanceId(),
				att.sessionId(),
				AttendanceStatus.valueOf(att.finalStatus())
			))
			.toList();

		return new MemberAttendanceResult(
			document.memberId(),
			records,
			new AttendanceStats(
				document.total(),
				document.present(),
				document.absent(),
				document.late(),
				document.leftEarly(),
				document.rate()
			)
		);
	}

}

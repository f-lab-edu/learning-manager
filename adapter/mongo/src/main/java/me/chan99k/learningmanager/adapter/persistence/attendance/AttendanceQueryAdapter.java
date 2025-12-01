package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;
import me.chan99k.learningmanager.application.attendance.AttendanceQueryRepository;
import me.chan99k.learningmanager.domain.attendance.Attendance;

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
	public List<Attendance> findByMemberId(Long memberId) {
		return repository.findByMemberId(memberId)
			.stream()
			.map(AttendanceDocument::toDomain)
			.toList();

	}

	@Override
	public List<Attendance> findByMemberIdAndSessionIds(Long memberId, List<Long> sessionIds) {
		if (sessionIds.isEmpty()) {
			return List.of();
		}

		return repository.findByMemberIdAndSessionIdIn(memberId, sessionIds)
			.stream()
			.map(AttendanceDocument::toDomain)
			.toList();
	}

	@Override
	public List<AttendanceProjection> findAttendanceProjectionByMemberIdAndSessionIds(Long memberId,
		List<Long> sessionIds) {
		if (sessionIds.isEmpty()) {
			return List.of();
		}

		return repository.findProjectionByMemberIdAndSessionIds(memberId, sessionIds)
			.stream()
			.map(this::toAttendanceProjection)
			.toList();

	}

	private AttendanceProjection toAttendanceProjection(AttendanceDocument doc) {
		return new AttendanceProjection(
			doc.get_id().toString(),
			doc.getSessionId(),
			doc.getMemberId(),
			doc.getFinalStatus()
		);
	}

}

package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.attendance.requires.AttendanceQueryRepository;
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
}

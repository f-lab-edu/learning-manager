package me.chan99k.learningmanager.adapter.persistence.attendance;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.attendance.requires.AttendanceCommandRepository;
import me.chan99k.learningmanager.domain.attendance.Attendance;

@Repository
public class AttendanceCommandAdapter implements AttendanceCommandRepository {

	private final AttendanceMongoRepository repository;

	public AttendanceCommandAdapter(AttendanceMongoRepository attendanceMongoRepository) {
		this.repository = attendanceMongoRepository;
	}

	@Override
	public Attendance save(Attendance attendance) {
		AttendanceDocument attDoc = AttendanceDocument.from(attendance);
		AttendanceDocument save = repository.save(attDoc);

		return save.toDomain();
	}
}

package me.chan99k.learningmanager.application.attendance.requires;

import me.chan99k.learningmanager.domain.attendance.Attendance;

public interface AttendanceCommandRepository {
	Attendance save(Attendance attendance);
}

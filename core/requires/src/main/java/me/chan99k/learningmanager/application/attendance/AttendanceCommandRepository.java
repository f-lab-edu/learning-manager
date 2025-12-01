package me.chan99k.learningmanager.application.attendance;

import me.chan99k.learningmanager.domain.attendance.Attendance;

public interface AttendanceCommandRepository {
	Attendance save(Attendance attendance);
}

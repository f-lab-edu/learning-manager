package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public record CheckedOut(Instant timestamp) implements AttendanceEvent {
}

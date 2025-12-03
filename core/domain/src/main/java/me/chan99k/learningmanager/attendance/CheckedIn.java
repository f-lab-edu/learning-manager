package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public record CheckedIn(Instant timestamp) implements AttendanceEvent {
}

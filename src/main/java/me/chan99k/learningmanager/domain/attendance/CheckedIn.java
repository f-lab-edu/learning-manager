package me.chan99k.learningmanager.domain.attendance;

import java.time.Instant;

public record CheckedIn(Instant timestamp) implements AttendanceEvent {
}

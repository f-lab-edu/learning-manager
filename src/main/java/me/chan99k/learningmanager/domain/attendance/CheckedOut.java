package me.chan99k.learningmanager.domain.attendance;

import java.time.Instant;

public record CheckedOut(Instant timestamp) implements AttendanceEvent {
}

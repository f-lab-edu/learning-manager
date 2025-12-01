package me.chan99k.learningmanager.domain.attendance;

import java.time.Clock;
import java.time.Instant;

public sealed interface AttendanceEvent permits CheckedIn, CheckedOut {
	static CheckedIn checkIn(Clock clock) {
		return new CheckedIn(clock.instant());
	}

	static CheckedOut checkOut(Clock clock) {
		return new CheckedOut(clock.instant());
	}

	Instant timestamp();
}






package me.chan99k.learningmanager.qr;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.attendance.QRCodeGenerator;

@Component
public class SimpleQRCodeGenerator implements QRCodeGenerator {
	private final Clock clock;

	public SimpleQRCodeGenerator(Clock clock) {
		this.clock = clock;
	}

	public String generateQrCode(Long sessionId) {
		Instant expiry = clock.instant().plus(5, ChronoUnit.MINUTES);
		return String.format("SESSION_%d_%d", sessionId, expiry.toEpochMilli());
	}

	public boolean validateQrCode(String qrCode, Long sessionId) {
		if (qrCode == null || !qrCode.startsWith("SESSION_")) {
			return false;
		}

		try {
			String[] parts = qrCode.split("_");
			Long codeSessionId = Long.parseLong(parts[1]);
			var expiryMillis = Long.parseLong(parts[2]);

			return codeSessionId.equals(sessionId) && clock.instant().toEpochMilli() < expiryMillis;
		} catch (Exception e) {
			return false;
		}
	}

}

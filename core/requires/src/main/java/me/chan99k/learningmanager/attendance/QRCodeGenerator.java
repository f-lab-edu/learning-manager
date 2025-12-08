package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public interface QRCodeGenerator {
	/**
	 * QR 코드 생성
	 */
	String generateQrCode(Long sessionId, Instant expiresAt);

	/**
	 * QR 코드 검증
	 */
	boolean validateQrCode(String qrCode, Long sessionId);

}

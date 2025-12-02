package me.chan99k.learningmanager.attendance;

public interface QRCodeGenerator {
	/**
	 *  QR 코드 생성
	 */
	String generateQrCode(Long sessionId);

	/**
	 * QR 코드 검증
	 */
	boolean validateQrCode(String qrCode, Long sessionId);

}

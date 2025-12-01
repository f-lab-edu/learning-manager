package me.chan99k.learningmanager.application.member.requires;

public interface RefreshTokenProvider {
	String generateRefreshToken(Long memberId, String email);

	RefreshResult refreshAccessToken(String refreshToken);

	void revokeRefreshToken(String refreshToken);

	boolean validateRefreshToken(String refreshToken);

	record RefreshResult(String newAccessToken, String newRefreshToken) {
	}
}
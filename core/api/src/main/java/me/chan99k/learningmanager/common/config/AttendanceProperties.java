package me.chan99k.learningmanager.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "attendance.reservation")
public class AttendanceProperties {

	private int failureExpirationSeconds = 120;
	private int maxRetryCount = 3;
	private int stuckTimeoutSeconds = 600;

	public int getFailureExpirationSeconds() {
		return failureExpirationSeconds;
	}

	public void setFailureExpirationSeconds(int failureExpirationSeconds) {
		this.failureExpirationSeconds = failureExpirationSeconds;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public int getStuckTimeoutSeconds() {
		return stuckTimeoutSeconds;
	}

	public void setStuckTimeoutSeconds(int stuckTimeoutSeconds) {
		this.stuckTimeoutSeconds = stuckTimeoutSeconds;
	}
}
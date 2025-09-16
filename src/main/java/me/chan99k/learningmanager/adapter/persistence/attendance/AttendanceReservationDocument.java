package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.time.Instant;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "attendance_reservations")
public class AttendanceReservationDocument {

	@Id
	private ObjectId id;

	@Indexed(unique = true)
	private String sessionMemberKey;

	private Long sessionId;
	private Long memberId;
	private Instant reservedAt;
	private Instant expiresAt;
	private ReservationStatus status;
	private String operationId;
	private int retryCount;
	private String lastError;

	public AttendanceReservationDocument() {
	}

	public static AttendanceReservationDocument create(Long sessionId, Long memberId) {
		AttendanceReservationDocument doc = new AttendanceReservationDocument();
		doc.sessionMemberKey = sessionId + ":" + memberId;
		doc.sessionId = sessionId;
		doc.memberId = memberId;
		doc.reservedAt = Instant.now();
		doc.expiresAt = Instant.now().plusSeconds(300);
		doc.status = ReservationStatus.RESERVED;
		doc.operationId = UUID.randomUUID().toString();
		doc.retryCount = 0;
		return doc;
	}

	public void resetForReuse() {
		this.status = ReservationStatus.RESERVED;
		this.reservedAt = Instant.now();
		this.expiresAt = Instant.now().plusSeconds(300);
		this.operationId = UUID.randomUUID().toString();
		this.retryCount++;
		this.lastError = null;
	}

	public void markCommitted() {
		this.status = ReservationStatus.COMMITTED;
	}

	public void markFailed(String errorMessage) {
		this.status = ReservationStatus.FAILED;
		this.lastError = errorMessage;
		this.expiresAt = Instant.now().plusSeconds(120); // TODO : 만료 시간 설정으로 빼기
	}

	public boolean isReusable() {
		return this.status == ReservationStatus.FAILED && this.retryCount < 3; // TODO : 최대 재시도 횟수 설정으로 빼기
	}

	public boolean isStuck() {
		return this.status == ReservationStatus.RESERVED &&
			Instant.now().isAfter(this.reservedAt.plusSeconds(600)); // TODO : 타임아웃 시간 설정으로 빼기
	}

	public String getSessionMemberKey() {
		return sessionMemberKey;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public String getOperationId() {
		return operationId;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public Instant getReservedAt() {
		return reservedAt;
	}

	public enum ReservationStatus {
		RESERVED, COMMITTED, FAILED, COMPLETED
	}
}
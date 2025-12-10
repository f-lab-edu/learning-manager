package me.chan99k.learningmanager.adapter.persistence.attendance.documents;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import me.chan99k.learningmanager.attendance.Attendance;
import me.chan99k.learningmanager.attendance.AttendanceEvent;
import me.chan99k.learningmanager.attendance.AttendanceStatus;

@Document(collection = "attendances")
@CompoundIndex(name = "session_member_idx",
	def = "{'sessionId': 1, 'memberId': 1}",
	unique = true
)
public class AttendanceDocument {
	@Id
	private ObjectId _id;

	private Long sessionId;
	private Long memberId;
	private List<AttendanceEventDocument> events;
	private AttendanceStatus finalStatus;

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant lastModifiedAt;

	@CreatedBy
	private Long createdBy;

	@LastModifiedBy
	private Long lastModifiedBy;

	private AttendanceDocument() {
	}

	@PersistenceCreator
	private AttendanceDocument(ObjectId _id, Long sessionId, Long memberId,
		List<AttendanceEventDocument> events, AttendanceStatus finalStatus
	) {
		this._id = _id;
		this.sessionId = sessionId;
		this.memberId = memberId;
		this.events = events;
		this.finalStatus = finalStatus;
	}

	public static AttendanceDocument from(Attendance attendance) {
		ObjectId objectId = attendance.getId() != null ? new ObjectId(attendance.getId()) : null;

		List<AttendanceEventDocument> eventDocs = attendance.getEvents().stream()
			.map(AttendanceEventDocument::from)
			.toList();

		return new AttendanceDocument(
			objectId, attendance.getSessionId(), attendance.getMemberId(),
			eventDocs, attendance.getFinalStatus()
		);
	}

	public Attendance toDomain() {
		List<AttendanceEvent> domainEvents = this.events.stream()
			.map(AttendanceEventDocument::toDomain)
			.toList();

		return Attendance.restore(
			this._id != null ? this._id.toString() : null,
			this.sessionId,
			this.memberId,
			domainEvents
		);
	}

	public ObjectId get_id() {
		return _id;
	}

	public Long getSessionId() {
		return sessionId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public List<AttendanceEventDocument> getEvents() {
		return events;
	}

	public AttendanceStatus getFinalStatus() {
		return finalStatus;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastModifiedAt() {
		return lastModifiedAt;
	}

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}

	public Long getCreatedBy() {
		return createdBy;
	}



}

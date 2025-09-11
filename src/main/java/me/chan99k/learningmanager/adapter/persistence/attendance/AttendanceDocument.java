package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import me.chan99k.learningmanager.domain.attendance.Attendance;
import me.chan99k.learningmanager.domain.attendance.AttendanceEvent;
import me.chan99k.learningmanager.domain.attendance.AttendanceStatus;
import me.chan99k.learningmanager.domain.attendance.CheckedIn;
import me.chan99k.learningmanager.domain.attendance.CheckedOut;

@Document
@CompoundIndex(name = "session_member_idx",
	def = "{'sessionId': 1, 'memberId': 1}",
	unique = true
)
public class AttendanceDocument {
	private final Long sessionId;
	private final Long memberId;
	private final List<AttendanceEventDocument> events;
	private final AttendanceStatus finalStatus;
	@Id
	private ObjectId _id;

	@PersistenceCreator
	public AttendanceDocument(ObjectId _id, Long sessionId, Long memberId,
		List<AttendanceEventDocument> events, AttendanceStatus finalStatus) {
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

		return new AttendanceDocument(objectId, attendance.getSessionId(), attendance.getMemberId(),
			eventDocs, attendance.getFinalStatus());
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

	public record AttendanceEventDocument(
		String type,
		Instant timestamp
	) {
		public static AttendanceEventDocument from(AttendanceEvent event) {
			String type = event instanceof CheckedIn ? "CheckedIn" : "CheckedOut";

			return new AttendanceEventDocument(type, event.timestamp());
		}

		public AttendanceEvent toDomain() {
			return switch (this.type) {
				case "CheckedIn" -> new CheckedIn(this.timestamp);
				case "CheckedOut" -> new CheckedOut(this.timestamp);
				default -> throw new IllegalArgumentException("[System] 유효하지 않은 출석 이벤트 타입입니다: " + this.type);
			};
		}

	}

}

package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;

public interface AttendanceMongoRepository
	extends MongoRepository<AttendanceDocument, ObjectId>, CustomAttendanceMongoRepository {

	Optional<AttendanceDocument> findBySessionIdAndMemberId(Long sessionId, Long memberId);

	List<AttendanceDocument> findByMemberId(Long memberId);

	List<AttendanceDocument> findByMemberIdAndSessionIdIn(Long memberId, List<Long> sessionIds);

	record MemberAttendanceAggregationInfo(
		Long memberId,
		List<AttendanceRecordInfo> attendances,
		int total,
		int present,
		int absent,
		int late,
		int leftEarly,
		double rate
	) {
	}

	record AttendanceRecordInfo(
		String attendanceId,
		Long sessionId,
		String finalStatus
	) {
	}
}

package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceMongoRepository extends MongoRepository<AttendanceDocument, ObjectId> {
	Optional<AttendanceDocument> findBySessionIdAndMemberId(Long sessionId, Long memberId);
}

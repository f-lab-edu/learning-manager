package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;

public interface AttendanceMongoRepository extends MongoRepository<AttendanceDocument, ObjectId> {
	Optional<AttendanceDocument> findBySessionIdAndMemberId(Long sessionId, Long memberId);

	List<AttendanceDocument> findByMemberId(Long memberId);

	List<AttendanceDocument> findByMemberIdAndSessionIdIn(Long memberId, List<Long> sessionIds);

	/**
	 * 스칼라 프로젝션
	 */
	@Query(value = "{ 'memberId': ?0, 'sessionId': { $in: ?1 } }",
		fields = "{ '_id': 1, 'sessionId': 1, 'memberId': 1, 'finalStatus': 1 }")
	List<AttendanceDocument> findProjectionByMemberIdAndSessionIds(Long memberId, List<Long> sessionIds);

}

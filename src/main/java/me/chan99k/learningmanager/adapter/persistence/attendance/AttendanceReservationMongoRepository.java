package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AttendanceReservationMongoRepository extends MongoRepository<AttendanceReservationDocument, ObjectId> {

	Optional<AttendanceReservationDocument> findBySessionMemberKey(String sessionMemberKey);

	List<AttendanceReservationDocument> findByStatusAndReservedAtBefore(
		AttendanceReservationDocument.ReservationStatus status, Instant before);

	List<AttendanceReservationDocument> findByStatusAndRetryCountGreaterThan(
		AttendanceReservationDocument.ReservationStatus status, int retryCount);

	long countByStatus(AttendanceReservationDocument.ReservationStatus status);

	@Query("{ 'status': 'RESERVED', 'reservedAt': { $lt: ?0 } }")
	List<AttendanceReservationDocument> findStuckReservations(Instant cutoff);
}
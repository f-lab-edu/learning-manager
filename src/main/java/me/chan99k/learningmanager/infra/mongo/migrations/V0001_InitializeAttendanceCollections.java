package me.chan99k.learningmanager.infra.mongo.migrations;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.validation.Validator;

import com.mongodb.client.model.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "v0001-initialize-attendance-collections", order = "002", author = "learning-manager")
public class V0001_InitializeAttendanceCollections {

	private final Logger log = LoggerFactory.getLogger(V0001_InitializeAttendanceCollections.class);

	@Execution
	public void initializeAttendanceCollections(MongoTemplate mongoTemplate) {
		// 1. Attendance 컬렉션 생성 및 검증 규칙 설정
		if (!mongoTemplate.collectionExists("attendance")) {
			MongoJsonSchema attendanceSchema = MongoJsonSchema.builder()
				.required("sessionId", "memberId", "events", "finalStatus")
				.properties(
					JsonSchemaProperty.number("sessionId"),
					JsonSchemaProperty.number("memberId"),
					JsonSchemaProperty.array("events"),
					JsonSchemaProperty.string("finalStatus").possibleValues("PRESENT", "ABSENT", "LATE")
				).build();

			mongoTemplate.createCollection("attendance", CollectionOptions.empty()
				.validator(Validator.schema(attendanceSchema))
			);
		}

		// 2. reservation 컬렉션
		if (!mongoTemplate.collectionExists("attendance_reservations")) {

			MongoJsonSchema reservationSchema = MongoJsonSchema.builder()
				.required("sessionMemberKey", "sessionId", "memberId", "reservedAt", "expiresAt")
				.properties(
					JsonSchemaProperty.string("sessionMemberKey"),
					JsonSchemaProperty.number("sessionId"),
					JsonSchemaProperty.number("memberId"),
					JsonSchemaProperty.date("reservedAt"),
					JsonSchemaProperty.date("expiresAt")
				).build();

			mongoTemplate.createCollection("attendance_reservations", CollectionOptions.empty()
				.validator(Validator.schema(reservationSchema))
			);

			// TTL 인덱스 - 5분 후 자동 만료
			mongoTemplate.indexOps("attendance_reservations")
				.ensureIndex(new Index("expiresAt", Sort.Direction.ASC)
					.expire(0, TimeUnit.SECONDS)
					.named("ttl_expiration_idx"));

			// 유니크 키 인덱스 (중복 예약 방지)
			mongoTemplate.indexOps("attendance_reservations")
				.ensureIndex(new Index("sessionMemberKey", Sort.Direction.ASC)
					.unique()
					.named("session_member_key_unique_idx"));
		}

		if (!mongoTemplate.collectionExists("system_metadata")) {
			mongoTemplate.createCollection("system_metadata");
		}

		Document attendanceMetadata = new Document()
			.append("_id", "attendance_metadata")
			.append("version", "1.0.0")
			.append("supportedEvents", List.of("CheckedIn", "CheckedOut"))
			.append("supportedStatuses", List.of("PRESENT", "ABSENT", "LATE"))
			.append("reservationTimeoutMinutes", 5)
			.append("createdAt", new Date())
			.append("description", "Attendance system configuration and metadata");

		mongoTemplate.save(attendanceMetadata, "system_metadata");

		createAttendanceStatisticsView(mongoTemplate);
	}

	private void createAttendanceStatisticsView(MongoTemplate mongoTemplate) {
		try {
			// 출석 통계 뷰 생성 (세션별 출석률 계산용)
			mongoTemplate.execute(db -> {
				List<Document> pipeline = List.of(
					Document.parse("{ $group: { " +
						"_id: '$sessionId', " +
						"totalAttendees: { $sum: 1 }, " +
						"presentCount: { $sum: { $cond: [{ $eq: ['$finalStatus', 'PRESENT'] }, 1, 0] } }, " +
						"absentCount: { $sum: { $cond: [{ $eq: ['$finalStatus', 'ABSENT'] }, 1, 0] } }, " +
						"lateCount: { $sum: { $cond: [{ $eq: ['$finalStatus', 'LATE'] }, 1, 0] } } " +
						"} }"),
					Document.parse("{ $addFields: { " +
						"attendanceRate: { $divide: ['$presentCount', '$totalAttendees'] }, " +
						"lastUpdated: new Date() " +
						"} }")
				);

				db.createView("attendance_statistics_by_session", "attendance", pipeline);
				return null;
			});
		} catch (Exception e) {
			log.error("Warning: Failed to create attendance statistics view: {}", e.getMessage());
		}
	}

	@RollbackExecution
	public void rollbackInitialization(MongoTemplate mongoTemplate) {
		try {
			mongoTemplate.dropCollection("attendance_reservations");

			mongoTemplate.dropCollection("attendance_statistics_by_session");

			mongoTemplate.getCollection("system_metadata").deleteOne(Filters.eq("_id", "attendance_metadata"));

			// attendance 컬렉션은 실제 데이터가 있을 수 있으므로 삭제하지 않기, 대신 validator만 제거
			mongoTemplate.execute(db -> {
				db.runCommand(new Document("collMod", "attendance")
					.append("validator", new Document())
					.append("validationLevel", "off"));
				return null;
			});

		} catch (Exception e) {
			log.error("Warning: Partial rollback failure: {}", e.getMessage());
		}
	}
}
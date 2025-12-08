package me.chan99k.learningmanager.adapter.persistence.attendance;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.attendance.AttendanceStatus;

@Repository
public class CustomAttendanceMongoRepositoryImpl implements CustomAttendanceMongoRepository {

	private static final String COLLECTION_NAME = "attendances";

	private final MongoTemplate mongoTemplate;

	public CustomAttendanceMongoRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public AttendanceMongoRepository.MemberAttendanceAggregationInfo aggregateMemberAttendance(
		Long memberId, List<Long> sessionIds
	) {
		MatchOperation match = match(
			Criteria.where("memberId").is(memberId)
				.and("sessionId").in(sessionIds)
		);

		Aggregation aggregation = newAggregation(match, buildGroupByMember(), buildProjection());

		AggregationResults<AttendanceMongoRepository.MemberAttendanceAggregationInfo> results = mongoTemplate.aggregate(
			aggregation, COLLECTION_NAME,
			AttendanceMongoRepository.MemberAttendanceAggregationInfo.class);

		return results.getUniqueMappedResult();
	}

	@Override
	public List<AttendanceMongoRepository.MemberAttendanceAggregationInfo> aggregateAllMembersAttendance(
		List<Long> sessionIds, List<Long> memberIds
	) {
		MatchOperation match = match(
			Criteria.where("sessionId").in(sessionIds)
				.and("memberId").in(memberIds)
		);

		Aggregation aggregation = newAggregation(
			match, buildGroupByMember(), buildProjection()
		);

		AggregationResults<AttendanceMongoRepository.MemberAttendanceAggregationInfo> results = mongoTemplate.aggregate(
			aggregation, COLLECTION_NAME,
			AttendanceMongoRepository.MemberAttendanceAggregationInfo.class);

		return results.getMappedResults();
	}

	/**
	 * $group stage: memberId 별 출석 집계
	 */
	private GroupOperation buildGroupByMember() {
		return group("memberId")
			.push(new org.bson.Document()
				.append("attendanceId", new org.bson.Document("$toString", "$_id"))
				.append("sessionId", "$sessionId")
				.append("finalStatus", "$finalStatus")
			).as("attendances")
			.count().as("total")
			.sum(conditionalCount(AttendanceStatus.PRESENT.name())).as("present")
			.sum(conditionalCount(AttendanceStatus.ABSENT.name())).as("absent")
			.sum(conditionalCount(AttendanceStatus.LATE.name())).as("late")
			.sum(conditionalCount(AttendanceStatus.LEFT_EARLY.name())).as("leftEarly");
	}

	/**
	 * $project stage: 출석률 계산 및 필드 매핑
	 */
	private ProjectionOperation buildProjection() {
		return project()
			.and("_id").as("memberId")
			.andInclude("attendances", "total", "present", "absent", "late", "leftEarly")
			.and(ConditionalOperators
				.when(Criteria.where("total").is(0))
				.then(0.0)
				.otherwise(
					// (present / total) * 100
					org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Multiply
						.valueOf(
							org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide
								.valueOf("present").divideBy("total")
						).multiplyBy(100)
				)
			).as("rate");
	}

	private ConditionalOperators.Cond conditionalCount(String status) {
		return ConditionalOperators
			.when(Criteria.where("finalStatus").is(status))
			.then(1)
			.otherwise(0);
	}
}

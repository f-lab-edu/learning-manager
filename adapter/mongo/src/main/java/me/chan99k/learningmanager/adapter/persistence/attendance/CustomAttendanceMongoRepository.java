package me.chan99k.learningmanager.adapter.persistence.attendance;

import java.util.List;

public interface CustomAttendanceMongoRepository {
	AttendanceMongoRepository.MemberAttendanceAggregationInfo aggregateMemberAttendance(
		Long memberId, List<Long> sessionIds
	);

	List<AttendanceMongoRepository.MemberAttendanceAggregationInfo> aggregateAllMembersAttendance(
		List<Long> sessionIds,
		List<Long> memberIds
	);
}

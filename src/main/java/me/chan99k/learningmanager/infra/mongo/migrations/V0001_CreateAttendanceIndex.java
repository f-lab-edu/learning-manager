package me.chan99k.learningmanager.infra.mongo.migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "v0001-create-attendance-index", order = "001", author = "learning-manager")
public class V0001_CreateAttendanceIndex {

	private final Logger log = LoggerFactory.getLogger(V0001_CreateAttendanceIndex.class);

	@Execution
	public void createAttendanceIndexes(MongoTemplate mongoTemplate) {
		// 1. 복합 유니크 인덱스: (sessionId, memberId) - 중복 출석 방지
		mongoTemplate.indexOps("attendance")
			.ensureIndex(new CompoundIndexDefinition(
				new org.bson.Document()
					.append("sessionId", 1)
					.append("memberId", 1)
			).unique().named("session_member_unique_idx"));

		// 2. 세션별 조회 최적화 인덱스
		mongoTemplate.indexOps("attendance")
			.ensureIndex(new Index("sessionId", Sort.Direction.ASC)
				.named("session_idx"));

		// 3. 생성 시간 기반 조회 최적화 (최근 출석 조회용)
		mongoTemplate.indexOps("attendance")
			.ensureIndex(new Index("createdAt", Sort.Direction.DESC)
				.named("created_at_desc_idx"));

		// 4. 이벤트 타입별 분석 쿼리 최적화
		mongoTemplate.indexOps("attendance")
			.ensureIndex(new Index("events.type", Sort.Direction.ASC)
				.named("event_type_idx"));

		// 5. 최종 상태별 조회 최적화
		mongoTemplate.indexOps("attendance")
			.ensureIndex(new Index("finalStatus", Sort.Direction.ASC)
				.named("final_status_idx"));
	}

	@RollbackExecution
	public void rollbackAttendanceIndexes(MongoTemplate mongoTemplate) {
		try {
			mongoTemplate.indexOps("attendance").dropIndex("session_member_unique_idx");
			mongoTemplate.indexOps("attendance").dropIndex("session_idx");
			mongoTemplate.indexOps("attendance").dropIndex("created_at_desc_idx");
			mongoTemplate.indexOps("attendance").dropIndex("event_type_idx");
			mongoTemplate.indexOps("attendance").dropIndex("final_status_idx");
		} catch (Exception e) {
			// 인덱스 삭제 실패는 로그만 남기고 계속 진행
			// TODO :: 실제 환경에서는 로깅 프레임워크 사용 필요
			log.error("Failed to rollback indexes: {}", e.getMessage());
		}
	}
}
package me.chan99k.learningmanager.infra.mongo.migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "v0002-add-member-id-index", order = "002", author = "learning-manager")
public class V0002_AddMemberIdIndex {

	private final Logger log = LoggerFactory.getLogger(V0002_AddMemberIdIndex.class);

	@Execution
	public void addMemberIdIndex(MongoTemplate mongoTemplate) {
		// memberId 단일 인덱스 - findByMemberId 쿼리용
		mongoTemplate.indexOps("attendance")
			.ensureIndex(new Index("memberId", Sort.Direction.ASC)
				.named("member_idx"));

		log.info("Added memberId index for attendance collection");
	}

	@RollbackExecution
	public void rollbackMemberIdIndex(MongoTemplate mongoTemplate) {
		try {
			mongoTemplate.indexOps("attendance").dropIndex("member_idx");
			log.info("Dropped memberId index from attendance collection");
		} catch (Exception e) {
			log.error("Failed to rollback memberId index: {}", e.getMessage());
		}
	}
}

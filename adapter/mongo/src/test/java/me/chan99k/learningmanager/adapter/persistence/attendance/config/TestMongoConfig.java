package me.chan99k.learningmanager.adapter.persistence.attendance.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;

import jakarta.annotation.PostConstruct;
import me.chan99k.learningmanager.adapter.persistence.attendance.documents.AttendanceDocument;

@TestConfiguration
public class TestMongoConfig {

	private final MongoTemplate mongoTemplate;

	public TestMongoConfig(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@PostConstruct
	public void createIndexes() {
		mongoTemplate.indexOps(AttendanceDocument.class)
			.ensureIndex(new CompoundIndexDefinition(
				new org.bson.Document()
					.append("sessionId", 1)
					.append("memberId", 1)
			).unique().named("session_member_unique_idx"));

		mongoTemplate.indexOps(AttendanceDocument.class)
			.ensureIndex(new Index("sessionId", Sort.Direction.ASC)
				.named("session_idx"));

		mongoTemplate.indexOps(AttendanceDocument.class)
			.ensureIndex(new Index("memberId", Sort.Direction.ASC)
				.named("member_idx"));
	}
}
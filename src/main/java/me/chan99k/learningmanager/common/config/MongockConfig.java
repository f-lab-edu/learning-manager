package me.chan99k.learningmanager.common.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.runner.springboot.MongockSpringboot;
import io.mongock.runner.springboot.base.MongockApplicationRunner;

@Configuration
public class MongockConfig {

	@Bean
	public MongockApplicationRunner mongockApplicationRunner(ApplicationContext springContext,
		MongoTemplate mongoTemplate) {
		return MongockSpringboot.builder()
			.setDriver(io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver.withDefaultLock(mongoTemplate))
			.addMigrationScanPackage("me.chan99k.learningmanager.infra.mongo.migrations")
			.setSpringContext(springContext)
			.setTransactionEnabled(false)
			.setTrackIgnored(false)
			.buildApplicationRunner();
	}
}
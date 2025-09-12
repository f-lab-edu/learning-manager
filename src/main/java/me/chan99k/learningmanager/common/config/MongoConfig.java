package me.chan99k.learningmanager.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import io.mongock.runner.springboot.EnableMongock;

@Configuration
@EnableMongoAuditing
@EnableMongock
@EnableMongoRepositories(basePackages = "me.chan99k.learningmanager.adapter.persistence.attendance")
public class MongoConfig {
}

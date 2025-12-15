package me.chan99k.learningmanager.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;

@Configuration
public class JdbcConfig {

	@Bean
	public SQLTemplates sqlTemplates() {
		return MySQLTemplates.builder().build();
	}

	@Bean
	public SQLQueryFactory sqlQueryFactory(DataSource dataSource, SQLTemplates sqlTemplates) {
		return new SQLQueryFactory(new com.querydsl.sql.Configuration(sqlTemplates), dataSource);
	}
}

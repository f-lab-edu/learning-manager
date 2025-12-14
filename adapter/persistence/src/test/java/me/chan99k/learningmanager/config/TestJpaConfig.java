package me.chan99k.learningmanager.config;

import java.util.Optional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@TestConfiguration
@EnableJpaAuditing
public class TestJpaConfig {
	@Bean
	public AuditorAware<Long> auditorAware() {
		return () -> Optional.of(1L);
	}

	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
		return new JPAQueryFactory(entityManager);
	}
}

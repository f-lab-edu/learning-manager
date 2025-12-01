package me.chan99k.learningmanager.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "me.chan99k.learningmanager.adapter.persistence")
public class JpaConfig {
	@Bean
	public AuditorAware<Long> auditorAware() {
		return () -> Optional.of(1L);
	}
}

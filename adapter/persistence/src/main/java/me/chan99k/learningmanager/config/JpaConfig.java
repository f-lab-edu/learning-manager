package me.chan99k.learningmanager.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import me.chan99k.learningmanager.member.SystemRoleHierarchy;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "me.chan99k.learningmanager")
public class JpaConfig {

	@Bean
	public AuditorAware<Long> auditorAware() {
		return () -> Optional.of(1L);
	}

	@Bean
	public SystemRoleHierarchy systemRoleHierarchy() {
		return new SystemRoleHierarchy();
	}

}

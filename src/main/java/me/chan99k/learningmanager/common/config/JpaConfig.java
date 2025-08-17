package me.chan99k.learningmanager.common.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
	// TODO :: Spring Security 추가하여 사용자 정보를 사용할 수 있도록 수정 필요
	@Bean
	public AuditorAware<Long> auditorAware() {
		return () -> Optional.of(1L); // 시스템 관리 계정
	}
}

package me.chan99k.learningmanager.common.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
	"me.chan99k.learningmanager.adapter.persistence.course",
	"me.chan99k.learningmanager.adapter.persistence.member",
	"me.chan99k.learningmanager.adapter.persistence.session"}
)
public class JpaConfig {
	// TODO :: Spring Security 추가하여 사용자 정보를 사용할 수 있도록 수정 필요
	// TODO :: 다른 Spring Data Repositories 들도 사용하므로 Audit 설정 빈을 별도의 파일로 이동 필요
	@Bean
	public AuditorAware<Long> auditorAware() {
		return () -> Optional.of(1L); // 시스템 관리 계정
	}
}

package me.chan99k.learningmanager.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class TestSecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll() // 테스트에서는 모든 요청 허용
			)
			.headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

		return httpSecurity.build();
	}

	@Bean
	public DelegatingSecurityContextAsyncTaskExecutor securityContextAsyncTaskExecutor(
		@Qualifier("memberTaskExecutor") AsyncTaskExecutor memberTaskExecutor
	) {
		return new DelegatingSecurityContextAsyncTaskExecutor(memberTaskExecutor);
	}
}
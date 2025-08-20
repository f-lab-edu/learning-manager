package me.chan99k.learningmanager.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import me.chan99k.learningmanager.adapter.auth.JwtAuthenticationFilter;

@Configuration
@Profile("!test")
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		httpSecurity
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터 추가
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.POST, "api/v1/members/register").permitAll()
				.requestMatchers(HttpMethod.POST, "api/v1/members/auth/token").permitAll()
				.requestMatchers(HttpMethod.POST, "api/v1/members/activate").authenticated()
				.requestMatchers(HttpMethod.GET, "/api/v1/members/*/profile-public").permitAll()
				.requestMatchers("/h2-console/**").permitAll()
				.anyRequest().authenticated()
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpStatus.UNAUTHORIZED.value());
					response.setContentType("application/problem+json");
					response.getWriter()
						.write(
							"{\"type\":\"https://api.lm.com/errors/authentication\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"[System] 사용자 인증이 필요합니다\",\"code\":\"UNAUTHORIZED\"}");
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					response.setStatus(HttpStatus.FORBIDDEN.value());
					response.setContentType("application/problem+json");
					response.getWriter()
						.write(
							"{\"type\":\"https://api.lm.com/errors/authorization\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"[System] 접근 권한이 없습니다\",\"code\":\"FORBIDDEN\"}");
				})
			)
			.headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.cors(Customizer.withDefaults());

		return httpSecurity.build();
	}

	@Bean
	public DelegatingSecurityContextAsyncTaskExecutor securityContextAsyncTaskExecutor(
		@Qualifier("memberTaskExecutor") AsyncTaskExecutor memberTaskExecutor
	) {
		return new DelegatingSecurityContextAsyncTaskExecutor(memberTaskExecutor);
	}

}

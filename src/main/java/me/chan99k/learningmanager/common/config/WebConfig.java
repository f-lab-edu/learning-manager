package me.chan99k.learningmanager.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import me.chan99k.learningmanager.adapter.auth.JwtAuthenticationFilter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public WebConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
		FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(jwtAuthenticationFilter);

		registration.addUrlPatterns("/api/v1/members/profile");
		registration.addUrlPatterns("/api/v1/members/password");
		registration.addUrlPatterns("/api/v1/attendance/*");
		registration.addUrlPatterns("/api/v1/courses/*");
		// 개인 일정 조회
		registration.addUrlPatterns("/api/v1/sessions/members/*");
		// 참여자 관리
		registration.addUrlPatterns("/api/v1/sessions/*/participants*");
		// 캘린더 조회 (비즈니스 민감 정보 보호)
		registration.addUrlPatterns("/api/v1/sessions/calendar");
		registration.setOrder(1);

		return registration;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("*")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true);
	}
}
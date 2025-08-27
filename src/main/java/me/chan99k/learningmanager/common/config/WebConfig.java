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
		// 인증이 필요한 경로만 지정 (인증 불필요: /register, /auth/*, /activate)
		registration.addUrlPatterns("/api/v1/members/profile/*");
		registration.addUrlPatterns("/api/v1/admin/*");
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
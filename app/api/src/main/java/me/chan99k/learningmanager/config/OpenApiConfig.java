package me.chan99k.learningmanager.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(apiInfo())
			.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
			.components(new Components()
				.addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()));
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("1. Public API")
			.displayName("Public API")
			.pathsToMatch("/api/v1/auth/**", "/api/v1/members/register", "/api/v1/members/activate")
			.build();
	}

	@Bean
	public GroupedOpenApi privateApi() {
		return GroupedOpenApi.builder()
			.group("2. Private API")
			.displayName("Private API")
			.pathsToMatch("/api/v1/**")
			.pathsToExclude("/api/v1/auth/**", "/api/v1/members/register", "/api/v1/members/activate")
			.build();
	}

	private Info apiInfo() {
		return new Info()
			.title("Learning Manager API")
			.description("학습 관리 시스템 API 문서")
			.version("v1.0.0")
			.contact(new Contact()
				.name("chan99k")
				.url("https://github.com/f-lab-edu/learning-manager"));
	}

	private SecurityScheme createSecurityScheme() {
		return new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");
	}
}
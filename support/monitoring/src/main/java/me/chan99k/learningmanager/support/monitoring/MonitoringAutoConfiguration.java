package me.chan99k.learningmanager.support.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:monitoring-defaults.yml", factory = YamlPropertySourceFactory.class)
public class MonitoringAutoConfiguration {

	@Bean
	public HealthIndicator applicationHealthIndicator() {
		return () -> Health.up()
			.withDetail("application", "learning-manager")
			.build();
	}
}

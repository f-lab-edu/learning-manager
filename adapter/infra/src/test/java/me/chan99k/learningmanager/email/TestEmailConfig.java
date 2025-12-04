package me.chan99k.learningmanager.email;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import me.chan99k.learningmanager.member.EmailSender;

@TestConfiguration
public class TestEmailConfig {

	@Bean
	public EmailSender emailSender() {
		return new ConsoleEmailSender();
	}
}

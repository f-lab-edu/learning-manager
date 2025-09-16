package me.chan99k.learningmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LearningManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningManagerApplication.class, args);
	}

}

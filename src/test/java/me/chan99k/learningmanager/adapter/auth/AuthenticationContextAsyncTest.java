package me.chan99k.learningmanager.adapter.auth;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=test")
public class AuthenticationContextAsyncTest {
	Logger log = LoggerFactory.getLogger(AuthenticationContextAsyncTest.class);
	@Autowired
	@Qualifier("memberTaskExecutor")
	private Executor memberTaskExecutor;

	@Test
	@DisplayName("비동기 작업으로 인증 컨텍스트가 전파되는지 확인")
	void test01() throws ExecutionException, InterruptedException {
		Long expectedMemberId = 123L;

		log.info("HTTP Thread Member ID: {}", expectedMemberId);

		AuthenticationContextHolder.setCurrentMemberId(expectedMemberId);

		// CompletableFuture로 비동기 작업 실행
		CompletableFuture<Long> asyncResult = CompletableFuture.supplyAsync(() -> {
			Long asyncMemberId = AuthenticationContextHolder.getCurrentMemberId().orElse(null);
			log.info("Async Thread Member ID: {}", asyncMemberId);
			return asyncMemberId;
		}, memberTaskExecutor);

		// 비동기 스레드에서도 동일한 Member ID 조회가 가능하여야 한다
		Long actualMemberId = asyncResult.get();
		assertThat(actualMemberId).isEqualTo(expectedMemberId);

		AuthenticationContextHolder.clear();
	}
}

package me.chan99k.learningmanager.adapter.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;

import me.chan99k.learningmanager.application.auth.requires.UserContext;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=test")
public class AuthenticationContextAsyncTest {
	Logger log = LoggerFactory.getLogger(AuthenticationContextAsyncTest.class);
	@Autowired
	@Qualifier("memberTaskExecutor")
	private Executor memberTaskExecutor;

	@Autowired
	private UserContext userContext;

	@Test
	@DisplayName("비동기 작업으로 인증 컨텍스트가 전파되는지 확인")
	void test01() throws ExecutionException, InterruptedException {
		Long expectedMemberId = 123L;

		log.info("HTTP Thread Member ID: {}", expectedMemberId);

		// Mock JWT와 Authentication 설정
		Jwt mockJwt = mock(Jwt.class);
		when(mockJwt.getSubject()).thenReturn(expectedMemberId.toString());

		Authentication mockAuth = mock(Authentication.class);
		when(mockAuth.isAuthenticated()).thenReturn(true);
		when(mockAuth.getPrincipal()).thenReturn(mockJwt);

		SecurityContext mockSecurityContext = mock(SecurityContext.class);
		when(mockSecurityContext.getAuthentication()).thenReturn(mockAuth);

		SecurityContextHolder.setContext(mockSecurityContext);

		// CompletableFuture로 비동기 작업 실행
		CompletableFuture<Long> asyncResult = CompletableFuture.supplyAsync(() -> {
			Long asyncMemberId = userContext.getCurrentMemberId();
			log.info("Async Thread Member ID: {}", asyncMemberId);
			return asyncMemberId;
		}, memberTaskExecutor);

		// 비동기 스레드에서도 동일한 Member ID 조회가 가능하여야 한다
		Long actualMemberId = asyncResult.get();
		assertThat(actualMemberId).isEqualTo(expectedMemberId);

		SecurityContextHolder.clearContext();
	}
}

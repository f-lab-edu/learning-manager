package me.chan99k.learningmanager.common.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import me.chan99k.learningmanager.adapter.auth.AuthenticationContextTaskDecorator;

@Configuration
@EnableAsync
public class AsyncConfig {
	private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

	@Bean(name = "memberTaskExecutor")
	public AsyncTaskExecutor memberTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		/* ============== 필수 스레드 풀 설정 ============== */

		// 코어 스레드 수: I/O 집약적 작업(DB 저장, 조회)을 위해 CPU 코어 수보다 많이 설정
		// 항상 활성 상태로 유지되는 최소 스레드 개수
		executor.setCorePoolSize(5);

		// 최대 스레드 수: 부하 증가 시 확장할 수 있는 최대 스레드 수
		// I/O 대기가 많을 것이므로 코어 수의 배수로 확장 허용
		executor.setMaxPoolSize(20);

		// 큐 용량: 모든 스레드가 사용 중일 때 대기할 수 있는 작업 수 (참고 : LinkedBlockingQueue)
		executor.setQueueCapacity(50);

		/* ============== 스레드 관리 설정 ============== */

		// 스레드 이름 접두사: 로그 추적 및 모니터링을 위한 식별자
		executor.setThreadNamePrefix("member-async-");

		// 유휴 스레드 유지 시간: 코어 스레드 외 추가 생성된 스레드의 최대 유휴 시간(초)
		// 불필요한 스레드를 정리하여 리소스 절약
		executor.setKeepAliveSeconds(60);

		// 코어 스레드 타임아웃 허용: 유휴 상태의 코어 스레드도 일정 시간 후 종료 허용
		// 메모리 사용량 최적화, 하지만 스레드 생성 비용 증가 트레이드오프
		// false로 설정 시 코어 스레드는 항상 유지됨
		executor.setAllowCoreThreadTimeOut(true);

		// 스레드 우선순위: 1(최저)~10(최고), 기본값 5, OS 스케줄러에 의존
		executor.setThreadPriority(Thread.NORM_PRIORITY);

		// 데몬 스레드 설정: true 시 JVM 종료를 방해하지 않음
		// false(기본값): 일반 스레드로 JVM 종료 시 완료 대기
		executor.setDaemon(false);

		/* ============== 태스크 실행 관련 설정 ============== */

		// 태스크 데코레이터: 각 태스크 실행 전/후 공통 처리 로직 추가 가능
		// 예: Mapped Diagnostic Context 복사, 메트릭 수집 등
		addAuthenticationContextTaskDecorator(executor);

		/* ============== 거부 정책 설정  ============== */
		// 거부 정책: 큐가 가득 차고 최대 스레드 수에 도달했을 때의 처리 방식
		// 1. CallerRunsPolicy: 요청한 스레드(Tomcat 스레드)에서 직접 실행하여 작업 스레드 풀에 공간이 생길 때까지 요청을 자연스럽게 줄이는 효과
		// 2. AbortPolicy: RejectedExecutionException 예외 발생 (기본값)
		// 3. DiscardPolicy: 조용히 무시하고 버림
		// 4. DiscardOldestPolicy: 큐에서 가장 오래된 작업 제거 후 새 작업 추가
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		/* ============== 애플리케이션 종료 관련 설정  ============== */

		// 애플리케이션 종료 시 대기 시간: 진행 중인 작업 완료를 위한 최대 대기 시간(초)
		// 이 시간 후에도 완료되지 않으면 강제 종료
		executor.setAwaitTerminationSeconds(60);

		// 종료 시 작업 완료 대기: 애플리케이션 종료 시 진행 중인 작업들이 완료될 때까지 대기
		// false 시 즉시 종료, true 시 awaitTerminationSeconds 만큼 대기
		executor.setWaitForTasksToCompleteOnShutdown(true);

		/* ============== 성능 튜닝 설정  ============== */

		// 스레드 풀 사전 시작: 애플리케이션 시작 시 코어 스레드들을 미리 생성
		// false(기본값): 태스크가 제출될 때 스레드 생성 / true: 초기화 시점에 모든 코어 스레드 생성
		executor.setPrestartAllCoreThreads(false);

		// JMX를 통한 모니터링 활성화를 위한 빈 이름 설정
		// Spring Boot Actuator와 함께 사용하면 /actuator/metrics에서 확인 가능
		executor.setBeanName("memberTaskExecutor");

		executor.initialize();

		log.info(
			"[System] Member Task Executor initialized - Core: {}, Max: {}, Queue: {}, KeepAlive: {}s, Priority: {}, Daemon: {}",
			executor.getCorePoolSize(),
			executor.getMaxPoolSize(),
			executor.getQueueCapacity(),
			executor.getKeepAliveSeconds(),
			executor.getThreadPriority(),
			executor.isDaemon()
		);

		return executor;
	}

	@Bean(name = "emailTaskExecutor")
	public AsyncTaskExecutor emailTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("email-async-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

		// 이메일 스레드는 낮은 우선순위로 설정 (다른 비즈니스 로직보다 후순위)
		executor.setThreadPriority(Thread.NORM_PRIORITY - 1);

		executor.setAllowCoreThreadTimeOut(true);
		executor.setKeepAliveSeconds(120);
		executor.setBeanName("emailTaskExecutor");
		executor.setTaskDecorator(new AuthenticationContextTaskDecorator());
		executor.initialize();

		log.info(
			"[System] Email Task Executor initialized - Core: {}, Max: {}, Queue: {}, Priority: {}, KeepAlive: {}s",
			executor.getCorePoolSize(),
			executor.getMaxPoolSize(),
			executor.getQueueCapacity(),
			executor.getThreadPriority(),
			executor.getKeepAliveSeconds());

		return executor;
	}

	@Bean(name = "courseTaskExecutor")
	public AsyncTaskExecutor courseTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("course-async-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

		addAuthenticationContextTaskDecorator(executor);

		executor.setAllowCoreThreadTimeOut(true);
		executor.setKeepAliveSeconds(120);
		executor.setBeanName("courseTaskExecutor");
		executor.initialize();

		log.info(
			"[System] Course Task Executor initialized - Core: {}, Max: {}, Queue: {}, Priority: {}, KeepAlive: {}s",
			executor.getCorePoolSize(),
			executor.getMaxPoolSize(),
			executor.getQueueCapacity(),
			executor.getThreadPriority(),
			executor.getKeepAliveSeconds());

		return executor;
	}

	@Bean(name = "sessionTaskExecutor")
	public AsyncTaskExecutor sessionTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("session-async-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

		addAuthenticationContextTaskDecorator(executor);

		executor.setAllowCoreThreadTimeOut(true);
		executor.setKeepAliveSeconds(120);
		executor.setBeanName("sessionTaskExecutor");
		executor.initialize();

		log.info(
			"[System] Session Task Executor initialized - Core: {}, Max: {}, Queue: {}, Priority: {}, KeepAlive: {}s",
			executor.getCorePoolSize(),
			executor.getMaxPoolSize(),
			executor.getQueueCapacity(),
			executor.getThreadPriority(),
			executor.getKeepAliveSeconds());

		return executor;
	}

	private void addAuthenticationContextTaskDecorator(ThreadPoolTaskExecutor executor) {
		executor.setTaskDecorator(runnable -> {
			Runnable authDecoratedTask = new AuthenticationContextTaskDecorator().decorate(runnable);

			String currentThreadName = Thread.currentThread().getName();

			return () -> {
				try {
					log.debug("Task started by thread: {} -> executing in: {}",
						currentThreadName, Thread.currentThread().getName());
					authDecoratedTask.run();
				} finally {
					log.debug("Task completed in thread: {}", Thread.currentThread().getName());
				}
			};
		});
	}
}

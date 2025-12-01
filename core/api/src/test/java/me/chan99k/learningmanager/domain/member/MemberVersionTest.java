package me.chan99k.learningmanager.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import me.chan99k.learningmanager.config.TestJpaConfig;

/**
 * 낙관적 락의 단순 동작 여부를 테스트하기 위한 테스트 클래스.
 * 영속성 계층 구현 이후에는 테스트케이스들을 영속성 계층 테스트에 통합하고 이 클래스는 삭제할 예정
 */
@DisplayName("Member 엔티티 테스트")
@DataJpaTest
@Import(TestJpaConfig.class)
class MemberVersionTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private EntityManagerFactory emf;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Nested
	class VersionTest {
		private Member member;

		@BeforeEach
		void setUp() {
			member = Member.registerDefault(() -> "testNickname");
			em.persistAndFlush(member);
		}

		@Test
		@DisplayName("member를 수정하면 버전이 1 증가한다.")
		void version_increases_when_member_modified() {
			assertThat(member.getVersion()).isEqualTo(0L);

			member.updateProfile("updating-url", "updating-intro");
			em.flush();
			em.clear();

			Member foundMember = em.find(Member.class, member.getId());
			assertThat(foundMember.getVersion()).isEqualTo(1L);
		}
	}

	@Nested
	@DisplayName("레이스 컨디션에서의 버전 정합성 체크")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	class ConcurrencyTest {
		private TransactionTemplate transactionTemplate;
		private Long memberId;

		@BeforeEach
		void setUp() {
			transactionTemplate = new TransactionTemplate(transactionManager);

			memberId = transactionTemplate.execute(status -> {
				try (var setupEm = emf.createEntityManager()) {
					setupEm.getTransaction().begin();
					Member member = Member.registerDefault(() -> "testNickname");
					setupEm.persist(member);
					setupEm.getTransaction().commit();
					return member.getId();
				}
			});
		}

		@AfterEach
		void tearDown() {
			transactionTemplate.execute(status -> {
				try (var cleanupEm = emf.createEntityManager()) {
					cleanupEm.getTransaction().begin();
					Member member = cleanupEm.find(Member.class, memberId);
					if (member != null) {
						cleanupEm.remove(member);
					}
					cleanupEm.getTransaction().commit();
				}
				return null;
			});
		}

		@Test
		@DisplayName("여러 스레드가 member를 동시에 수정할 때, 오직 1개의 스레드만 성공하고 나머지는 실패한다.")
		void optimistic_lock_works_under_concurrency() throws InterruptedException {
			int threadCount = 100;
			ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
			CountDownLatch countDownLatch = new CountDownLatch(threadCount);

			final CyclicBarrier barrier = new CyclicBarrier(threadCount);

			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failureCount = new AtomicInteger(0);

			for (int i = 0; i < threadCount; i++) {
				executorService.submit(() -> {
					var threadEm = emf.createEntityManager();
					try {
						threadEm.getTransaction().begin();
						Member threadMember = threadEm.find(Member.class, memberId);

						barrier.await(); // 모든 스레드 대기

						threadMember.updateProfile("URL-" + UUID.randomUUID(), "INTRO-" + UUID.randomUUID());
						threadEm.getTransaction().commit();
						successCount.incrementAndGet();

					} catch (ObjectOptimisticLockingFailureException e) {
						failureCount.incrementAndGet();
						if (threadEm.getTransaction().isActive()) {
							threadEm.getTransaction().rollback();
						}
					} catch (RollbackException e) {
						if (e.getCause() instanceof OptimisticLockException) {
							failureCount.incrementAndGet();
						} else {
							e.printStackTrace();
						}
					} catch (Exception e) {
						if (threadEm.getTransaction().isActive()) {
							threadEm.getTransaction().rollback();
						}
						e.printStackTrace();
					} finally {
						countDownLatch.countDown();
						threadEm.close();
					}
				});
			}

			countDownLatch.await();
			executorService.shutdown();

			Long finalVersion = transactionTemplate.execute(status -> {
				try (var finalEm = emf.createEntityManager()) {
					return finalEm.find(Member.class, memberId).getVersion();
				}
			});

			assertThat(finalVersion).isEqualTo(1L);
			assertThat(successCount.get()).isEqualTo(1L);
			assertThat(failureCount.get()).isEqualTo(threadCount - 1L);
		}
	}
}
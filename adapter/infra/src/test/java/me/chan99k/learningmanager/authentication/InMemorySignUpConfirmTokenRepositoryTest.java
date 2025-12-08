package me.chan99k.learningmanager.authentication;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InMemorySignUpConfirmTokenRepositoryTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_TOKEN = "test-token-123";

	InMemorySignUpConfirmTokenRepository repository;

	@BeforeEach
	void setUp() {
		repository = new InMemorySignUpConfirmTokenRepository(3600L);
	}

	@Nested
	@DisplayName("save 메서드")
	class SaveTest {

		@Test
		@DisplayName("[Success] 토큰과 이메일을 저장하면 조회할 수 있다")
		void save_then_findable() {
			repository.save(TEST_TOKEN, TEST_EMAIL);

			Optional<String> result = repository.findEmailByToken(TEST_TOKEN);

			assertThat(result).isPresent();
			assertThat(result.get()).isEqualTo(TEST_EMAIL);
		}
	}

	@Nested
	@DisplayName("findEmailByToken 메서드")
	class FindEmailByTokenTest {

		@Test
		@DisplayName("[Success] 존재하지 않는 토큰은 빈 Optional을 반환한다")
		void notFound_returnsEmpty() {
			Optional<String> result = repository.findEmailByToken("non-existent-token");

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("[Success] 저장된 토큰은 이메일을 반환한다")
		void found_returnsEmail() {
			repository.save(TEST_TOKEN, TEST_EMAIL);

			Optional<String> result = repository.findEmailByToken(TEST_TOKEN);

			assertThat(result).contains(TEST_EMAIL);
		}
	}

	@Nested
	@DisplayName("existsAndNotExpired 메서드")
	class ExistsAndNotExpiredTest {

		@Test
		@DisplayName("[Success] 존재하고 만료되지 않은 토큰은 true를 반환한다")
		void exists_returnsTrue() {
			repository.save(TEST_TOKEN, TEST_EMAIL);

			boolean result = repository.existsAndNotExpired(TEST_TOKEN);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Success] 존재하지 않는 토큰은 false를 반환한다")
		void notExists_returnsFalse() {
			boolean result = repository.existsAndNotExpired("non-existent-token");

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("delete 메서드")
	class DeleteTest {

		@Test
		@DisplayName("[Success] 토큰을 삭제하면 더 이상 조회되지 않는다")
		void delete_then_notFound() {
			repository.save(TEST_TOKEN, TEST_EMAIL);

			repository.delete(TEST_TOKEN);

			assertThat(repository.findEmailByToken(TEST_TOKEN)).isEmpty();
		}
	}

	@Nested
	@DisplayName("만료 처리")
	class ExpirationTest {

		@Test
		@DisplayName("[Success] 만료된 토큰은 조회 시 빈 Optional을 반환한다")
		void expired_returnsEmpty() {
			InMemorySignUpConfirmTokenRepository shortLivedRepo =
				new InMemorySignUpConfirmTokenRepository(0L);
			shortLivedRepo.save(TEST_TOKEN, TEST_EMAIL);

			Optional<String> result = shortLivedRepo.findEmailByToken(TEST_TOKEN);

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("[Success] 만료된 토큰은 isExpired가 true를 반환한다")
		void expired_isExpiredReturnsTrue() {
			InMemorySignUpConfirmTokenRepository shortLivedRepo =
				new InMemorySignUpConfirmTokenRepository(0L);
			shortLivedRepo.save(TEST_TOKEN, TEST_EMAIL);

			boolean result = shortLivedRepo.isExpired(TEST_TOKEN);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Success] 만료되지 않은 토큰은 isExpired가 false를 반환한다")
		void notExpired_isExpiredReturnsFalse() {
			repository.save(TEST_TOKEN, TEST_EMAIL);

			boolean result = repository.isExpired(TEST_TOKEN);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("[Success] 존재하지 않는 토큰은 isExpired가 false를 반환한다")
		void notExists_isExpiredReturnsFalse() {
			boolean result = repository.isExpired("non-existent-token");

			assertThat(result).isFalse();
		}
	}
}

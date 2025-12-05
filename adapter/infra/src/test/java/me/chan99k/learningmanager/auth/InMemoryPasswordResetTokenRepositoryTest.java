package me.chan99k.learningmanager.auth;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.chan99k.learningmanager.member.Email;

class InMemoryPasswordResetTokenRepositoryTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_TOKEN = "reset-token-123";
	private static final long VALIDITY_SECONDS = 300L;

	InMemoryPasswordResetTokenRepository repository;

	@BeforeEach
	void setUp() {
		repository = new InMemoryPasswordResetTokenRepository(VALIDITY_SECONDS);
	}

	@Nested
	@DisplayName("save 메서드")
	class SaveTest {

		@Test
		@DisplayName("토큰과 이메일을 저장한다")
		void saves_token_and_email() {
			repository.save(TEST_TOKEN, Email.of(TEST_EMAIL));

			Optional<String> result = repository.findEmailByToken(TEST_TOKEN);
			assertThat(result).contains(TEST_EMAIL);
		}
	}

	@Nested
	@DisplayName("findEmailByToken 메서드")
	class FindEmailByTokenTest {

		@Test
		@DisplayName("저장된 이메일을 조회한다")
		void finds_saved_email() {
			repository.save(TEST_TOKEN, Email.of(TEST_EMAIL));

			Optional<String> result = repository.findEmailByToken(TEST_TOKEN);

			assertThat(result).isPresent();
			assertThat(result.get()).isEqualTo(TEST_EMAIL);
		}

		@Test
		@DisplayName("존재하지 않는 토큰은 빈 Optional을 반환한다")
		void returns_empty_for_nonexistent_token() {
			Optional<String> result = repository.findEmailByToken("non-existent-token");

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("만료된 토큰은 빈 Optional을 반환한다")
		void returns_empty_for_expired_token() {
			InMemoryPasswordResetTokenRepository shortLivedRepo =
				new InMemoryPasswordResetTokenRepository(0L);
			shortLivedRepo.save(TEST_TOKEN, Email.of(TEST_EMAIL));

			Optional<String> result = shortLivedRepo.findEmailByToken(TEST_TOKEN);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("existsAndNotExpired 메서드")
	class ExistsAndNotExpiredTest {

		@Test
		@DisplayName("존재하고 만료되지 않은 토큰은 true를 반환한다")
		void returns_true_for_valid_token() {
			repository.save(TEST_TOKEN, Email.of(TEST_EMAIL));

			boolean result = repository.existsAndNotExpired(TEST_TOKEN);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("존재하지 않는 토큰은 false를 반환한다")
		void returns_false_for_nonexistent_token() {
			boolean result = repository.existsAndNotExpired("non-existent-token");

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("만료된 토큰은 false를 반환한다")
		void returns_false_for_expired_token() {
			InMemoryPasswordResetTokenRepository shortLivedRepo =
				new InMemoryPasswordResetTokenRepository(0L);
			shortLivedRepo.save(TEST_TOKEN, Email.of(TEST_EMAIL));

			boolean result = shortLivedRepo.existsAndNotExpired(TEST_TOKEN);

			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("delete 메서드")
	class DeleteTest {

		@Test
		@DisplayName("토큰을 삭제하면 더 이상 조회되지 않는다")
		void deletes_token() {
			repository.save(TEST_TOKEN, Email.of(TEST_EMAIL));

			repository.delete(TEST_TOKEN);

			assertThat(repository.findEmailByToken(TEST_TOKEN)).isEmpty();
		}

		@Test
		@DisplayName("존재하지 않는 토큰 삭제 시 예외가 발생하지 않는다")
		void does_not_throw_for_nonexistent_token() {
			assertThatCode(() -> repository.delete("non-existent-token"))
				.doesNotThrowAnyException();
		}
	}
}

package me.chan99k.learningmanager.auth;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InMemoryRefreshTokenRepositoryTest {

	private static final Long MEMBER_ID = 1L;
	private static final Long MEMBER_ID_2 = 2L;
	private static final String TOKEN_VALUE = "test-refresh-token";
	private static final String TOKEN_VALUE_2 = "test-refresh-token-2";
	private static final Duration TTL = Duration.ofHours(24);

	InMemoryRefreshTokenRepository repository;

	@BeforeEach
	void setUp() {
		repository = new InMemoryRefreshTokenRepository();
	}

	private RefreshToken createValidToken(Long memberId, String tokenValue) {
		return new RefreshToken(
			null,
			tokenValue,
			memberId,
			Instant.now().plus(TTL),
			Instant.now(),
			false
		);
	}

	private RefreshToken createExpiredToken(Long memberId, String tokenValue) {
		return new RefreshToken(
			null,
			tokenValue,
			memberId,
			Instant.now().minus(Duration.ofHours(1)),
			Instant.now().minus(Duration.ofHours(25)),
			false
		);
	}

	@Nested
	@DisplayName("save 메서드")
	class SaveTest {

		@Test
		@DisplayName("토큰을 저장하면 조회할 수 있다")
		void saves_token_and_findable() {
			RefreshToken token = createValidToken(MEMBER_ID, TOKEN_VALUE);

			repository.save(token);

			Optional<RefreshToken> result = repository.findByToken(TOKEN_VALUE);
			assertThat(result).isPresent();
			assertThat(result.get().getMemberId()).isEqualTo(MEMBER_ID);
		}

		@Test
		@DisplayName("같은 memberId로 저장하면 기존 토큰을 교체한다")
		void replaces_existing_token_for_same_member() {
			RefreshToken firstToken = createValidToken(MEMBER_ID, TOKEN_VALUE);
			RefreshToken secondToken = createValidToken(MEMBER_ID, TOKEN_VALUE_2);

			repository.save(firstToken);
			repository.save(secondToken);

			assertThat(repository.findByToken(TOKEN_VALUE)).isEmpty();
			assertThat(repository.findByToken(TOKEN_VALUE_2)).isPresent();
		}
	}

	@Nested
	@DisplayName("findByToken 메서드")
	class FindByTokenTest {

		@Test
		@DisplayName("존재하는 토큰을 조회한다")
		void finds_existing_token() {
			RefreshToken token = createValidToken(MEMBER_ID, TOKEN_VALUE);
			repository.save(token);

			Optional<RefreshToken> result = repository.findByToken(TOKEN_VALUE);

			assertThat(result).isPresent();
			assertThat(result.get().getToken()).isEqualTo(TOKEN_VALUE);
		}

		@Test
		@DisplayName("존재하지 않는 토큰은 빈 Optional을 반환한다")
		void returns_empty_for_nonexistent_token() {
			Optional<RefreshToken> result = repository.findByToken("non-existent-token");

			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("revokeByToken 메서드")
	class RevokeByTokenTest {

		@Test
		@DisplayName("토큰을 폐기하면 isRevoked가 true가 된다")
		void revokes_token() {
			RefreshToken token = createValidToken(MEMBER_ID, TOKEN_VALUE);
			repository.save(token);

			repository.revokeByToken(TOKEN_VALUE);

			Optional<RefreshToken> result = repository.findByToken(TOKEN_VALUE);
			assertThat(result).isPresent();
			assertThat(result.get().isRevoked()).isTrue();
		}

		@Test
		@DisplayName("존재하지 않는 토큰 폐기 시 예외가 발생하지 않는다")
		void does_not_throw_for_nonexistent_token() {
			assertThatCode(() -> repository.revokeByToken("non-existent-token"))
				.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("revokeAllByMemberId 메서드")
	class RevokeAllByMemberIdTest {

		@Test
		@DisplayName("memberId로 모든 토큰을 폐기한다")
		void revokes_all_tokens_by_member_id() {
			RefreshToken token = createValidToken(MEMBER_ID, TOKEN_VALUE);
			repository.save(token);

			repository.revokeAllByMemberId(MEMBER_ID);

			Optional<RefreshToken> result = repository.findByToken(TOKEN_VALUE);
			assertThat(result).isPresent();
			assertThat(result.get().isRevoked()).isTrue();
		}

		@Test
		@DisplayName("다른 memberId의 토큰은 영향받지 않는다")
		void does_not_affect_other_members_tokens() {
			RefreshToken token1 = createValidToken(MEMBER_ID, TOKEN_VALUE);
			RefreshToken token2 = createValidToken(MEMBER_ID_2, TOKEN_VALUE_2);
			repository.save(token1);
			repository.save(token2);

			repository.revokeAllByMemberId(MEMBER_ID);

			assertThat(repository.findByToken(TOKEN_VALUE).get().isRevoked()).isTrue();
			assertThat(repository.findByToken(TOKEN_VALUE_2).get().isRevoked()).isFalse();
		}
	}

	@Nested
	@DisplayName("deleteExpiredTokens 메서드")
	class DeleteExpiredTokensTest {

		@Test
		@DisplayName("만료된 토큰을 삭제한다")
		void deletes_expired_tokens() {
			RefreshToken expiredToken = createExpiredToken(MEMBER_ID, TOKEN_VALUE);
			repository.save(expiredToken);

			int deletedCount = repository.deleteExpiredTokens();

			assertThat(deletedCount).isEqualTo(1);
			assertThat(repository.findByToken(TOKEN_VALUE)).isEmpty();
		}

		@Test
		@DisplayName("폐기된 토큰을 삭제한다")
		void deletes_revoked_tokens() {
			RefreshToken token = createValidToken(MEMBER_ID, TOKEN_VALUE);
			repository.save(token);
			repository.revokeByToken(TOKEN_VALUE);

			int deletedCount = repository.deleteExpiredTokens();

			assertThat(deletedCount).isEqualTo(1);
			assertThat(repository.findByToken(TOKEN_VALUE)).isEmpty();
		}

		@Test
		@DisplayName("유효한 토큰은 삭제하지 않는다")
		void does_not_delete_valid_tokens() {
			RefreshToken validToken = createValidToken(MEMBER_ID, TOKEN_VALUE);
			repository.save(validToken);

			int deletedCount = repository.deleteExpiredTokens();

			assertThat(deletedCount).isEqualTo(0);
			assertThat(repository.findByToken(TOKEN_VALUE)).isPresent();
		}

		@Test
		@DisplayName("삭제된 토큰 수를 반환한다")
		void returns_deleted_count() {
			RefreshToken expiredToken1 = createExpiredToken(MEMBER_ID, TOKEN_VALUE);
			RefreshToken expiredToken2 = createExpiredToken(MEMBER_ID_2, TOKEN_VALUE_2);
			repository.save(expiredToken1);
			repository.save(expiredToken2);

			int deletedCount = repository.deleteExpiredTokens();

			assertThat(deletedCount).isEqualTo(2);
		}
	}
}

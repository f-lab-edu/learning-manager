package me.chan99k.learningmanager.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;

@ExtendWith(MockitoExtension.class)
class SignUpConfirmTokenProviderAdapterTest {

	private static final String TEST_EMAIL = "test@example.com";

	@Mock
	InMemorySignUpConfirmTokenRepository tokenRepository;

	SignUpConfirmTokenProviderAdapter tokenProvider;

	@BeforeEach
	void setUp() {
		tokenProvider = new SignUpConfirmTokenProviderAdapter(tokenRepository);
	}

	@Nested
	@DisplayName("createAndStoreToken 메서드")
	class CreateAndStoreTokenTest {

		@Test
		@DisplayName("[Success] 토큰을 생성하고 저장소에 저장한다")
		void creates_and_stores_token() {
			String token = tokenProvider.createAndStoreToken(TEST_EMAIL);

			assertThat(token).isNotNull();
			assertThat(token).isNotEmpty();
			then(tokenRepository).should().save(eq(token), eq(TEST_EMAIL));
		}

		@Test
		@DisplayName("[Success] 생성된 토큰은 URL-safe Base64 형식이다")
		void token_is_url_safe() {
			String token = tokenProvider.createAndStoreToken(TEST_EMAIL);

			assertThat(token).matches("^[A-Za-z0-9_-]+$");
		}

		@Test
		@DisplayName("[Success] 매번 다른 토큰이 생성된다")
		void generates_unique_tokens() {
			String token1 = tokenProvider.createAndStoreToken(TEST_EMAIL);
			String token2 = tokenProvider.createAndStoreToken(TEST_EMAIL);

			assertThat(token1).isNotEqualTo(token2);
		}
	}

	@Nested
	@DisplayName("validateAndGetEmail 메서드")
	class ValidateAndGetEmailTest {

		@Test
		@DisplayName("[Success] 유효한 토큰이면 이메일을 반환한다")
		void valid_token_returns_email() {
			String token = "valid-token";
			given(tokenRepository.isExpired(token)).willReturn(false);
			given(tokenRepository.findEmailByToken(token)).willReturn(Optional.of(TEST_EMAIL));

			String result = tokenProvider.validateAndGetEmail(token);

			assertThat(result).isEqualTo(TEST_EMAIL);
		}

		@Test
		@DisplayName("[Failure] 만료된 토큰이면 EXPIRED_ACTIVATION_TOKEN 예외를 던진다")
		void expired_token_throws_exception() {
			String token = "expired-token";
			given(tokenRepository.isExpired(token)).willReturn(true);

			assertThatThrownBy(() -> tokenProvider.validateAndGetEmail(token))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.EXPIRED_ACTIVATION_TOKEN);
		}

		@Test
		@DisplayName("[Failure] 존재하지 않는 토큰이면 INVALID_ACTIVATION_TOKEN 예외를 던진다")
		void invalid_token_throws_exception() {
			String token = "invalid-token";
			given(tokenRepository.isExpired(token)).willReturn(false);
			given(tokenRepository.findEmailByToken(token)).willReturn(Optional.empty());

			assertThatThrownBy(() -> tokenProvider.validateAndGetEmail(token))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(MemberProblemCode.INVALID_ACTIVATION_TOKEN);
		}
	}

	@Nested
	@DisplayName("removeToken 메서드")
	class RemoveTokenTest {

		@Test
		@DisplayName("[Success] 토큰을 저장소에서 삭제한다")
		void removes_token() {
			String token = "token-to-remove";

			tokenProvider.removeToken(token);

			then(tokenRepository).should().delete(token);
		}
	}

	@Nested
	@DisplayName("isValid 메서드")
	class IsValidTest {

		@Test
		@DisplayName("[Success] 유효한 토큰이면 true를 반환한다")
		void valid_token_returns_true() {
			String token = "valid-token";
			given(tokenRepository.existsAndNotExpired(token)).willReturn(true);

			boolean result = tokenProvider.isValid(token);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 유효하지 않은 토큰이면 false를 반환한다")
		void invalid_token_returns_false() {
			String token = "invalid-token";
			given(tokenRepository.existsAndNotExpired(token)).willReturn(false);

			boolean result = tokenProvider.isValid(token);

			assertThat(result).isFalse();
		}
	}
}

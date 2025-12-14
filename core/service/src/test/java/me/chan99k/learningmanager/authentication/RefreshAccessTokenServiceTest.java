package me.chan99k.learningmanager.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Account;
import me.chan99k.learningmanager.member.AccountStatus;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.MemberStatus;
import me.chan99k.learningmanager.member.Nickname;

@ExtendWith(MockitoExtension.class)
class RefreshAccessTokenServiceTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final Long MEMBER_ID = 1L;
	private static final String ORIGINAL_TOKEN = "original-refresh-token";
	private static final String NEW_ACCESS_TOKEN = "new-access-token";
	private static final int REFRESH_TOKEN_TTL_HOURS = 24;
	private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600L;

	@Mock
	RefreshTokenRepository refreshTokenRepository;
	@Mock
	MemberQueryRepository memberQueryRepository;
	@Mock
	JwtProvider jwtProvider;

	RefreshAccessTokenService refreshAccessTokenService;

	@BeforeEach
	void setUp() {
		refreshAccessTokenService = new RefreshAccessTokenService(
			refreshTokenRepository,
			memberQueryRepository,
			jwtProvider,
			REFRESH_TOKEN_TTL_HOURS
		);
	}

	private RefreshToken createValidRefreshToken() {
		return new RefreshToken(
			1L,
			ORIGINAL_TOKEN,
			MEMBER_ID,
			Instant.now().plus(Duration.ofHours(24)),
			Instant.now(),
			false
		);
	}

	private RefreshToken createExpiredRefreshToken() {
		return new RefreshToken(
			1L,
			ORIGINAL_TOKEN,
			MEMBER_ID,
			Instant.now().minus(Duration.ofHours(1)),
			Instant.now().minus(Duration.ofHours(25)),
			false
		);
	}

	private RefreshToken createRevokedRefreshToken() {
		return new RefreshToken(
			1L,
			ORIGINAL_TOKEN,
			MEMBER_ID,
			Instant.now().plus(Duration.ofHours(24)),
			Instant.now(),
			true
		);
	}

	private Member createTestMember() {
		Account account = Account.reconstitute(
			1L,
			AccountStatus.ACTIVE,
			Email.of(TEST_EMAIL),
			List.of(),
			Instant.now(),
			null,
			null,
			null,
			0L
		);

		return Member.reconstitute(
			MEMBER_ID,
			Email.of(TEST_EMAIL),
			Nickname.of("TestUser"),
			MemberStatus.ACTIVE,
			null,
			null,
			List.of(account),
			Instant.now(),
			null,
			null,
			null,
			0L
		);
	}

	@Nested
	@DisplayName("refresh 메서드")
	class RefreshTest {

		@Test
		@DisplayName("유효한 토큰으로 새 Access/Refresh Token을 발급한다")
		void issues_new_tokens_with_valid_refresh_token() {
			RefreshToken validToken = createValidRefreshToken();
			Member member = createTestMember();
			given(refreshTokenRepository.findByToken(ORIGINAL_TOKEN))
				.willReturn(Optional.of(validToken));
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.of(member));
			given(jwtProvider.createAccessToken(eq(MEMBER_ID), eq(TEST_EMAIL)))
				.willReturn(NEW_ACCESS_TOKEN);
			given(jwtProvider.getAccessTokenExpirationSeconds())
				.willReturn(ACCESS_TOKEN_EXPIRATION_SECONDS);

			RefreshAccessToken.Request request = new RefreshAccessToken.Request(ORIGINAL_TOKEN);
			RefreshAccessToken.Response response = refreshAccessTokenService.refresh(request);

			assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
			assertThat(response.refreshToken()).isNotNull();
			assertThat(response.refreshToken()).isNotEqualTo(ORIGINAL_TOKEN);
			assertThat(response.tokenType()).isEqualTo("Bearer");
		}

		@Test
		@DisplayName("존재하지 않는 토큰으로 요청하면 TOKEN_NOT_FOUND 예외를 던진다")
		void throws_exception_when_token_not_found() {
			given(refreshTokenRepository.findByToken(ORIGINAL_TOKEN))
				.willReturn(Optional.empty());

			RefreshAccessToken.Request request = new RefreshAccessToken.Request(ORIGINAL_TOKEN);

			assertThatThrownBy(() -> refreshAccessTokenService.refresh(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.TOKEN_NOT_FOUND);
		}

		@Test
		@DisplayName("폐기된 토큰으로 요청하면 REVOKED_TOKEN 예외를 던진다")
		void throws_exception_when_token_revoked() {
			RefreshToken revokedToken = createRevokedRefreshToken();
			given(refreshTokenRepository.findByToken(ORIGINAL_TOKEN))
				.willReturn(Optional.of(revokedToken));

			RefreshAccessToken.Request request = new RefreshAccessToken.Request(ORIGINAL_TOKEN);

			assertThatThrownBy(() -> refreshAccessTokenService.refresh(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.REVOKED_TOKEN);
		}

		@Test
		@DisplayName("만료된 토큰으로 요청하면 EXPIRED_TOKEN 예외를 던진다")
		void throws_exception_when_token_expired() {
			RefreshToken expiredToken = createExpiredRefreshToken();
			given(refreshTokenRepository.findByToken(ORIGINAL_TOKEN))
				.willReturn(Optional.of(expiredToken));

			RefreshAccessToken.Request request = new RefreshAccessToken.Request(ORIGINAL_TOKEN);

			assertThatThrownBy(() -> refreshAccessTokenService.refresh(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.EXPIRED_TOKEN);
		}

		@Test
		@DisplayName("회원 조회 실패 시 INVALID_TOKEN 예외를 던진다")
		void throws_exception_when_member_not_found() {
			RefreshToken validToken = createValidRefreshToken();
			given(refreshTokenRepository.findByToken(ORIGINAL_TOKEN))
				.willReturn(Optional.of(validToken));
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.empty());

			RefreshAccessToken.Request request = new RefreshAccessToken.Request(ORIGINAL_TOKEN);

			assertThatThrownBy(() -> refreshAccessTokenService.refresh(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.INVALID_TOKEN);
		}

		@Test
		@DisplayName("토큰 갱신 시 기존 토큰을 폐기한다 (Token Rotation)")
		void revokes_original_token_on_refresh() {
			RefreshToken validToken = createValidRefreshToken();
			Member member = createTestMember();
			given(refreshTokenRepository.findByToken(ORIGINAL_TOKEN))
				.willReturn(Optional.of(validToken));
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.of(member));
			given(jwtProvider.createAccessToken(eq(MEMBER_ID), eq(TEST_EMAIL)))
				.willReturn(NEW_ACCESS_TOKEN);
			given(jwtProvider.getAccessTokenExpirationSeconds())
				.willReturn(ACCESS_TOKEN_EXPIRATION_SECONDS);

			RefreshAccessToken.Request request = new RefreshAccessToken.Request(ORIGINAL_TOKEN);
			refreshAccessTokenService.refresh(request);

			then(refreshTokenRepository).should().revokeByToken(ORIGINAL_TOKEN);
		}

		@Test
		@DisplayName("토큰 갱신 시 새 Refresh Token을 저장한다")
		void saves_new_refresh_token() {
			RefreshToken validToken = createValidRefreshToken();
			Member member = createTestMember();
			given(refreshTokenRepository.findByToken(ORIGINAL_TOKEN))
				.willReturn(Optional.of(validToken));
			given(memberQueryRepository.findById(MEMBER_ID))
				.willReturn(Optional.of(member));
			given(jwtProvider.createAccessToken(eq(MEMBER_ID), eq(TEST_EMAIL)))
				.willReturn(NEW_ACCESS_TOKEN);
			given(jwtProvider.getAccessTokenExpirationSeconds())
				.willReturn(ACCESS_TOKEN_EXPIRATION_SECONDS);

			RefreshAccessToken.Request request = new RefreshAccessToken.Request(ORIGINAL_TOKEN);
			refreshAccessTokenService.refresh(request);

			ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
			then(refreshTokenRepository).should().save(captor.capture());

			RefreshToken savedToken = captor.getValue();
			assertThat(savedToken.getMemberId()).isEqualTo(MEMBER_ID);
			assertThat(savedToken.getToken()).isNotEqualTo(ORIGINAL_TOKEN);
			assertThat(savedToken.isRevoked()).isFalse();
		}
	}
}

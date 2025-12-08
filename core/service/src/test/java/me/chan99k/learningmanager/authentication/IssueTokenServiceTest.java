package me.chan99k.learningmanager.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import me.chan99k.learningmanager.member.Credential;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.MemberStatus;
import me.chan99k.learningmanager.member.Nickname;
import me.chan99k.learningmanager.member.SystemRole;

@ExtendWith(MockitoExtension.class)
class IssueTokenServiceTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PASSWORD = "Password123!";
	private static final String HASHED_PASSWORD = "hashed-password";
	private static final Long MEMBER_ID = 1L;
	private static final String ACCESS_TOKEN = "access-token";
	private static final String REFRESH_TOKEN_VALUE = "refresh-token";
	private static final int REFRESH_TOKEN_TTL_HOURS = 24;
	private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600L;

	@Mock
	MemberQueryRepository memberQueryRepository;
	@Mock
	PasswordEncoder passwordEncoder;
	@Mock
	JwtProvider jwtProvider;
	@Mock
	RefreshTokenRepository refreshTokenRepository;

	IssueTokenService issueTokenService;

	@BeforeEach
	void setUp() {
		issueTokenService = new IssueTokenService(
			memberQueryRepository,
			passwordEncoder,
			jwtProvider,
			refreshTokenRepository,
			REFRESH_TOKEN_TTL_HOURS
		);
	}

	private Member createTestMember() {
		Account account = Account.reconstitute(
			1L,
			AccountStatus.ACTIVE,
			Email.of(TEST_EMAIL),
			List.of(Credential.ofPassword(HASHED_PASSWORD)),
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
			SystemRole.MEMBER,
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
	@DisplayName("issueToken 메서드")
	class IssueTokenTest {

		@Test
		@DisplayName("올바른 이메일과 비밀번호로 토큰을 발급한다")
		void issues_token_with_valid_credentials() {
			Member member = createTestMember();
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD))
				.willReturn(true);
			given(jwtProvider.createAccessToken(eq(MEMBER_ID), eq(TEST_EMAIL), anyList()))
				.willReturn(ACCESS_TOKEN);
			given(jwtProvider.getAccessTokenExpirationSeconds())
				.willReturn(ACCESS_TOKEN_EXPIRATION_SECONDS);

			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);
			IssueToken.Response response = issueTokenService.issueToken(request);

			assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(response.refreshToken()).isNotNull();
		}

		@Test
		@DisplayName("존재하지 않는 이메일로 요청하면 INVALID_CREDENTIALS 예외를 던진다")
		void throws_exception_when_email_not_found() {
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.empty());

			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);

			assertThatThrownBy(() -> issueTokenService.issueToken(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.INVALID_CREDENTIALS);
		}

		@Test
		@DisplayName("잘못된 비밀번호로 요청하면 INVALID_CREDENTIALS 예외를 던진다")
		void throws_exception_when_password_invalid() {
			Member member = createTestMember();
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD))
				.willReturn(false);

			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);

			assertThatThrownBy(() -> issueTokenService.issueToken(request))
				.isInstanceOf(DomainException.class)
				.extracting("problemCode")
				.isEqualTo(AuthProblemCode.INVALID_CREDENTIALS);
		}

		@Test
		@DisplayName("토큰 발급 시 RefreshToken을 저장소에 저장한다")
		void saves_refresh_token_to_repository() {
			Member member = createTestMember();
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD))
				.willReturn(true);
			given(jwtProvider.createAccessToken(eq(MEMBER_ID), eq(TEST_EMAIL), anyList()))
				.willReturn(ACCESS_TOKEN);
			given(jwtProvider.getAccessTokenExpirationSeconds())
				.willReturn(ACCESS_TOKEN_EXPIRATION_SECONDS);

			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);
			issueTokenService.issueToken(request);

			ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
			then(refreshTokenRepository).should().save(captor.capture());

			RefreshToken savedToken = captor.getValue();
			assertThat(savedToken.getMemberId()).isEqualTo(MEMBER_ID);
			assertThat(savedToken.isRevoked()).isFalse();
		}

		@Test
		@DisplayName("응답에 tokenType으로 Bearer를 포함한다")
		void response_contains_bearer_token_type() {
			Member member = createTestMember();
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD))
				.willReturn(true);
			given(jwtProvider.createAccessToken(eq(MEMBER_ID), eq(TEST_EMAIL), anyList()))
				.willReturn(ACCESS_TOKEN);
			given(jwtProvider.getAccessTokenExpirationSeconds())
				.willReturn(ACCESS_TOKEN_EXPIRATION_SECONDS);

			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);
			IssueToken.Response response = issueTokenService.issueToken(request);

			assertThat(response.tokenType()).isEqualTo("Bearer");
		}

		@Test
		@DisplayName("응답에 expiresIn 값을 포함한다")
		void response_contains_expires_in() {
			Member member = createTestMember();
			given(memberQueryRepository.findByEmail(Email.of(TEST_EMAIL)))
				.willReturn(Optional.of(member));
			given(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD))
				.willReturn(true);
			given(jwtProvider.createAccessToken(eq(MEMBER_ID), eq(TEST_EMAIL), anyList()))
				.willReturn(ACCESS_TOKEN);
			given(jwtProvider.getAccessTokenExpirationSeconds())
				.willReturn(ACCESS_TOKEN_EXPIRATION_SECONDS);

			IssueToken.Request request = new IssueToken.Request(TEST_EMAIL, TEST_PASSWORD);
			IssueToken.Response response = issueTokenService.issueToken(request);

			assertThat(response.expiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRATION_SECONDS);
		}
	}
}

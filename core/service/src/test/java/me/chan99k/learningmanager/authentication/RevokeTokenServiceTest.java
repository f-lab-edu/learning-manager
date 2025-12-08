package me.chan99k.learningmanager.authentication;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevokeTokenServiceTest {

	private static final String REFRESH_TOKEN = "refresh-token-to-revoke";

	@Mock
	RefreshTokenRepository refreshTokenRepository;

	RevokeTokenService revokeTokenService;

	@BeforeEach
	void setUp() {
		revokeTokenService = new RevokeTokenService(refreshTokenRepository);
	}

	@Nested
	@DisplayName("revoke 메서드")
	class RevokeTest {

		@Test
		@DisplayName("토큰 폐기 요청 시 저장소의 revokeByToken을 호출한다")
		void calls_revoke_by_token_on_repository() {
			RevokeToken.Request request = new RevokeToken.Request(REFRESH_TOKEN);

			revokeTokenService.revoke(request);

			then(refreshTokenRepository).should().revokeByToken(REFRESH_TOKEN);
		}

		@Test
		@DisplayName("tokenTypeHint가 있어도 revokeByToken을 호출한다")
		void calls_revoke_by_token_with_type_hint() {
			RevokeToken.Request request = new RevokeToken.Request(REFRESH_TOKEN, "refresh_token");

			revokeTokenService.revoke(request);

			then(refreshTokenRepository).should().revokeByToken(REFRESH_TOKEN);
		}
	}
}

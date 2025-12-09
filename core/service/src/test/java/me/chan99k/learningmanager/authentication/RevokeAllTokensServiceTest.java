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
class RevokeAllTokensServiceTest {

	private static final Long MEMBER_ID = 1L;

	@Mock
	RefreshTokenRepository refreshTokenRepository;

	RevokeAllTokensService revokeAllTokensService;

	@BeforeEach
	void setUp() {
		revokeAllTokensService = new RevokeAllTokensService(refreshTokenRepository);
	}

	@Nested
	@DisplayName("revokeAll 메서드")
	class RevokeAllTest {

		@Test
		@DisplayName("회원 ID로 전체 토큰 폐기를 요청하면 저장소의 revokeAllByMemberId를 호출한다")
		void calls_revoke_all_by_member_id_on_repository() {
			revokeAllTokensService.revokeAll(MEMBER_ID);

			then(refreshTokenRepository).should().revokeAllByMemberId(MEMBER_ID);
		}
	}
}
package me.chan99k.learningmanager.authentication;

import static me.chan99k.learningmanager.authentication.AuthProblemCode.*;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberQueryRepository;

@Service
@Transactional
public class RefreshAccessTokenService implements RefreshAccessToken {
	private final RefreshTokenRepository refreshTokenRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final JwtProvider jwtProvider;
	private final Duration refreshTokenTtl;

	public RefreshAccessTokenService(RefreshTokenRepository refreshTokenRepository,
		MemberQueryRepository memberQueryRepository, JwtProvider jwtProvider,
		@Value("${auth.refresh-token.ttl-hours}") int refreshTokenTtlHours
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.jwtProvider = jwtProvider;
		this.refreshTokenTtl = Duration.ofHours(refreshTokenTtlHours);
	}

	@Override
	public Response refresh(Request request) {
		// 1. Refresh Token 조회
		RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
			.orElseThrow(() -> new DomainException(TOKEN_NOT_FOUND));

		// 2. 토큰 상태 검증
		if (refreshToken.isRevoked()) {
			throw new DomainException(REVOKED_TOKEN);
		}
		if (refreshToken.isExpired()) {
			throw new DomainException(EXPIRED_TOKEN);
		}

		// 3. Member 조회
		Member member = memberQueryRepository.findById(refreshToken.getMemberId())
			.orElseThrow(() -> new DomainException(INVALID_TOKEN));

		// 4. 기존 Refresh Token 폐기 (Rotation)
		refreshTokenRepository.revokeByToken(request.refreshToken());

		// 5. 새 Access Token 발급
		String email = member.getPrimaryEmail().address();
		List<String> roles = List.of(member.getRole().name());
		String newAccessToken = jwtProvider.createAccessToken(
			member.getId(),
			email,
			roles
		);

		// 6. 새 Refresh Token 발급
		RefreshToken newRefreshToken = RefreshToken.create(member.getId(), refreshTokenTtl);
		refreshTokenRepository.save(newRefreshToken);

		return Response.of(
			newAccessToken,
			newRefreshToken.getToken(),
			jwtProvider.getAccessTokenExpirationSeconds()
		);
	}
}

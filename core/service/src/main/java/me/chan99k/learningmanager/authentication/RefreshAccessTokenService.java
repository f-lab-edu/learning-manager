package me.chan99k.learningmanager.authentication;

import static me.chan99k.learningmanager.authentication.AuthProblemCode.*;

import java.time.Duration;

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
		// Refresh Token 조회
		RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
			.orElseThrow(() -> new DomainException(TOKEN_NOT_FOUND));

		// 토큰 상태 검증
		if (refreshToken.isRevoked()) {
			throw new DomainException(REVOKED_TOKEN);
		}
		if (refreshToken.isExpired()) {
			throw new DomainException(EXPIRED_TOKEN);
		}

		// Member 조회
		Member member = memberQueryRepository.findById(refreshToken.getMemberId())
			.orElseThrow(() -> new DomainException(INVALID_TOKEN));

		// 기존 Refresh Token 폐기 (Rotation)
		refreshTokenRepository.revokeByToken(request.refreshToken());

		// 새 Access Token 발급 (Minimal JWT: 역할은 런타임에 조회)
		String email = member.getPrimaryEmail().address();
		String newAccessToken = jwtProvider.createAccessToken(
			member.getId(),
			email
		);

		// 새 Refresh Token 발급
		RefreshToken newRefreshToken = RefreshToken.create(member.getId(), refreshTokenTtl);
		refreshTokenRepository.save(newRefreshToken);

		return Response.of(
			newAccessToken,
			newRefreshToken.getToken(),
			jwtProvider.getAccessTokenExpirationSeconds()
		);
	}
}

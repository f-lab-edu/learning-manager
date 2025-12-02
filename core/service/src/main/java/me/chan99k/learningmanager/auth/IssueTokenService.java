package me.chan99k.learningmanager.auth;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;

@Service
public class IssueTokenService implements IssueToken {

	private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

	private final AccountQueryRepository accountQueryRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	public IssueTokenService(AccountQueryRepository accountQueryRepository, PasswordEncoder passwordEncoder,
		JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository) {
		this.accountQueryRepository = accountQueryRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtProvider = jwtProvider;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Override
	public Response issueToken(Request request) {
		// 1. 이메일로 계정 조회
		Member member = accountQueryRepository.findMemberByEmail(request.email())
			.orElseThrow(() -> new DomainException(AuthProblemCode.INVALID_CREDENTIALS));

		// 2. 비밀번호 검증
		String storedPassword = accountQueryRepository.findPasswordByEmail(request.email())
			.orElseThrow(() -> new DomainException(AuthProblemCode.INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), storedPassword)) {
			throw new DomainException(AuthProblemCode.INVALID_CREDENTIALS);
		}

		// 3. Access Token 생성
		List<String> roles = List.of(member.getRole().name());
		String accessToken = jwtProvider.createAccessToken(
			member.getId(),
			request.email(),
			roles
		);

		// 4. Refresh Token 생성 및 저장
		RefreshToken refreshToken = RefreshToken.create(member.getId(), REFRESH_TOKEN_TTL);
		refreshTokenRepository.save(refreshToken);

		// 5. 응답 반환
		return Response.of(
			accessToken,
			refreshToken.getToken(),
			jwtProvider.getAccessTokenExpirationSeconds()
		);
	}
}

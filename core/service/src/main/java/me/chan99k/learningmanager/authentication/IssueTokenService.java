package me.chan99k.learningmanager.authentication;

import static me.chan99k.learningmanager.authentication.AuthProblemCode.*;
import static me.chan99k.learningmanager.member.CredentialType.*;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Account;
import me.chan99k.learningmanager.member.Credential;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberQueryRepository;

@Service
@Transactional
public class IssueTokenService implements IssueToken {

	private final MemberQueryRepository memberQueryRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final Duration refreshTokenTtlHours;

	public IssueTokenService(MemberQueryRepository accountQueryRepository, PasswordEncoder passwordEncoder,
		JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository,
		@Value("${auth.refresh-token.ttl-hours}") int refreshTokenTtlHours) {
		this.memberQueryRepository = accountQueryRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtProvider = jwtProvider;
		this.refreshTokenRepository = refreshTokenRepository;
		this.refreshTokenTtlHours = Duration.ofHours(refreshTokenTtlHours);
	}

	/**
	 * JPA는 조회만 수행. RefreshToken은 인메모리/Redis에 저장됨.
	 */
	@Override
	@Transactional(readOnly = true)
	public Response issueToken(Request request) {
		// 1. 이메일로 Member 조회
		Email email = Email.of(request.email());
		Member member = memberQueryRepository.findByEmail(email)
			.orElseThrow(() -> new DomainException(INVALID_CREDENTIALS));

		// 2. Account에서 PASSWORD 타입 Credential 조회
		Account account = member.findAccountByEmail(email);
		Credential credential = account.findCredentialByType(PASSWORD);

		// 3. 비밀번호 검증
		if (!passwordEncoder.matches(request.password(), credential.getSecret())) {
			throw new DomainException(INVALID_CREDENTIALS);
		}

		// 4. Access Token 발급
		List<String> roles = List.of(member.getRole().name());
		String accessToken = jwtProvider.createAccessToken(
			member.getId(),
			account.getEmail().address(),
			roles
		);

		// 5. Refresh Token 발급 및 저장
		RefreshToken refreshToken = RefreshToken.create(member.getId(), refreshTokenTtlHours);
		refreshTokenRepository.save(refreshToken);

		return Response.of(
			accessToken,
			refreshToken.getToken(),
			jwtProvider.getAccessTokenExpirationSeconds()
		);
	}
}

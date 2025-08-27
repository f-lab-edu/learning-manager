package me.chan99k.learningmanager.adapter.auth;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import me.chan99k.learningmanager.common.exception.AuthenticateException;

@Component
public class JwtAuthenticationFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
		throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest)request;

		try {
			String token = resolveToken(httpRequest);

			log.debug("[System] Processing authentication for protected resource: {}", httpRequest.getRequestURI());

			// 토큰 검증 실패 시 접근 차단
			if (!jwtTokenProvider.validateToken(token)) {
				log.error("[System] Invalid token for protected resource: {}", httpRequest.getRequestURI());
				throw new AuthenticateException(AuthProblemCode.FAILED_TO_VALIDATE_TOKEN);
			}

			String memberId = jwtTokenProvider.getMemberIdFromToken(token);
			log.debug("[System] Member ID from token: {}", memberId);

			// AuthenticationContextHolder에 Member ID 설정
			AuthenticationContextHolder.setCurrentMemberId(Long.valueOf(memberId));

			log.debug("[System] Authentication successful for member: {}", memberId);

			// 인증 성공 시에만 다음 필터로 진행
			filterChain.doFilter(request, response);
		} catch (AuthenticateException e) {
			log.error("[System] Authentication filter error for URI {}: {}",
				httpRequest.getRequestURI(), e.getMessage());
			throw e;
		} catch (NumberFormatException e) {
			throw new AuthenticateException(AuthProblemCode.INVALID_TOKEN_SUBJECT, e);
		} finally {
			AuthenticationContextHolder.clear();    // 요청 처리 완료 후 컨텍스트 정리
		}
	}

	private String resolveToken(HttpServletRequest request) {
		var bearer = request.getHeader("Authorization");
		if (!StringUtils.hasText(bearer)) {
			log.error("[System] No Authorization header found for protected resource: {}",
				request.getRequestURI());
			throw new AuthenticateException(AuthProblemCode.MISSING_AUTHORIZATION_HEADER);
		}

		if (!bearer.startsWith(BEARER_PREFIX)) {
			log.error("[System] Authorization header does not start with Bearer for protected resource: {}",
				request.getRequestURI());
			throw new AuthenticateException(AuthProblemCode.INVALID_AUTHORIZATION_HEADER);
		}

		String token = bearer.substring(BEARER_PREFIX.length());
		if (!StringUtils.hasText(token)) {
			log.error("[System] Empty Bearer token found for protected resource: {}",
				request.getRequestURI());
			throw new AuthenticateException(AuthProblemCode.EMPTY_BEARER_TOKEN);
		}

		return token;
	}

}

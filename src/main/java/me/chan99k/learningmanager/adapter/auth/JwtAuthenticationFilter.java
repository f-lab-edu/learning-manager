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

			log.debug("[System] Processing authentication for URI: {}", httpRequest.getRequestURI());

			if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
				String memberId = jwtTokenProvider.getMemberIdFromToken(token);

				log.debug("[System] Member ID from token: {}", memberId);

				// AuthenticationContextHolder에 Member ID 설정
				AuthenticationContextHolder.setCurrentMemberId(Long.valueOf(memberId));

				log.debug("[System] Authentication successful for member: {}", memberId);

			} else {
				log.debug("[System] No valid token found for request: {}", httpRequest.getRequestURI());
			}

			filterChain.doFilter(request, response);
		} catch (AuthenticateException e) {
			log.error("[System] Authentication filter error for URI {}: {}",
				httpRequest.getRequestURI(), e.getMessage());
			throw new AuthenticateException(AuthProblemCode.FAILED_TO_AUTHENTICATE, e);
		} catch (NumberFormatException e) {
			throw new AuthenticateException(AuthProblemCode.INVALID_TOKEN_SUBJECT, e);
		} finally {
			// 요청 처리 완료 후 컨텍스트 정리
			AuthenticationContextHolder.clear();
		}
	}

	private String resolveToken(HttpServletRequest request) {
		var bearer = request.getHeader("Authorization");
		if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
			return bearer.substring(BEARER_PREFIX.length());
		}
		return null;
	}
}

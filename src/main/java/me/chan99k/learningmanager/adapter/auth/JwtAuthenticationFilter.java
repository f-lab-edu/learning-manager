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
			log.info("===== JWT FILTER DEBUG START =====");
			log.info("Request URI : {}", httpRequest.getRequestURI());
			log.info("Token: :{}", token);

			if (token != null && jwtTokenProvider.validateToken(token)) {
				String memberId = jwtTokenProvider.getMemberIdFromToken(token);
				log.info("Member ID from token: {}", memberId);

				// AuthenticationContextHolder에 Member ID 설정
				AuthenticationContextHolder.setCurrentMemberId(Long.valueOf(memberId));
				log.info("[System] Member ID {} 을 AuthenticationContext에 설정하였습니다.", memberId);
			} else {
				log.debug("[System] 토큰 검증이 실패하였거나 토큰의 값이 null 입니다");
			}
			log.info("===== JWT FILTER DEBUG ENDS =====");

			filterChain.doFilter(request, response);
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
		return null; // FIXME :: null 반환은 지양하도록 변경하기
	}
}

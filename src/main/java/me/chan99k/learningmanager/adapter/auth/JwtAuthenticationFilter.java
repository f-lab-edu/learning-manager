package me.chan99k.learningmanager.adapter.auth;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("!test")
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String token = resolveToken(request);
		log.info("===== JWT FILTER DEBUG START =====");
		log.info("Request URI : {}", request.getRequestURI());
		log.info("Token: :{}", token);

		if (token != null && jwtTokenProvider.validateToken(token)) {
			String memberId = jwtTokenProvider.getMemberIdFromToken(token);
			log.info("Member ID from token: {}", memberId);
			// TODO: 실제로는 Member 엔티티에서 권한 정보를 조회해야 함
			var authorities = Collections.singletonList(new SimpleGrantedAuthority("MEMBER"));
			// TODO: 실제로는 Password VO 가 필요함!!
			var authentication = new UsernamePasswordAuthenticationToken(
				memberId, null, authorities
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.info("[System] Authentication set 을 SecurityContext 에 캐시 하였습니다.");
		} else {
			log.debug("[System] 토큰 검증이 실패하였거나 토큰의 값이 null 입니다");
		}
		log.info("===== JWT FILTER DEBUG ENDS =====");
		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		var bearer = request.getHeader("Authorization");
		if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
			return bearer.substring(BEARER_PREFIX.length());
		}
		return null; // FIXME :: null 반환은 지양하도록 변경하기
	}
}

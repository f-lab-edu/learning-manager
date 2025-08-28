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

import jakarta.servlet.http.HttpServletResponse;
import me.chan99k.learningmanager.common.exception.AuthenticateException;
import me.chan99k.learningmanager.common.exception.ProblemCode;


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
		HttpServletResponse httpResponse = (HttpServletResponse)response;


		try {
			String token = resolveToken(httpRequest);

			log.debug("[System] Processing authentication for protected resource: {}", httpRequest.getRequestURI());

			// 토큰 검증 실패 시 접근 차단
			if (!jwtTokenProvider.validateToken(token)) {
				log.error("[System] Invalid token for protected resource: {}", httpRequest.getRequestURI());
				writeErrorResponse(httpResponse, AuthProblemCode.FAILED_TO_VALIDATE_TOKEN);
				return;
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
			writeErrorResponse(httpResponse, e.getProblemCode());
		} catch (NumberFormatException e) {
			writeErrorResponse(httpResponse, AuthProblemCode.INVALID_TOKEN_SUBJECT);
		} finally {
			AuthenticationContextHolder.clear();    // 요청 처리 완료 후 컨텍스트 정리
		}
	}

	private String resolveToken(HttpServletRequest request) throws AuthenticateException {
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

	private void writeErrorResponse(HttpServletResponse response, ProblemCode problemCode) throws IOException {
		response.setStatus(401);
		response.setContentType("application/problem+json");
		response.setCharacterEncoding("UTF-8");

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"type\":\"https://api.lm.com/errors/").append(problemCode.getCode()).append("\",");
		json.append("\"title\":\"Authentication Error\",");
		json.append("\"status\":401,");
		json.append("\"detail\":\"").append(escapeJsonString(problemCode.getMessage())).append("\",");
		json.append("\"code\":\"").append(problemCode.getCode()).append("\"");
		json.append("}");

		response.getWriter().write(json.toString());
	}

	private String escapeJsonString(String str) {
		return str.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}

}

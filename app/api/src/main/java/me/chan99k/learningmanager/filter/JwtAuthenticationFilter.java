package me.chan99k.learningmanager.filter;

import java.io.IOException;
import java.util.Set;

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
import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.security.CustomUserDetails;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtProvider jwtProvider;
	private final SystemAuthorizationPort systemAuthorizationPort;

	public JwtAuthenticationFilter(
		JwtProvider jwtProvider,
		SystemAuthorizationPort systemAuthorizationPort
	) {
		this.jwtProvider = jwtProvider;
		this.systemAuthorizationPort = systemAuthorizationPort;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String token = resolveToken(request);

		if (token != null && jwtProvider.isValid(token)) {
			JwtProvider.Claims claims = jwtProvider.validateAndGetClaims(token);

			Set<SystemRole> roles = systemAuthorizationPort.getRoles(claims.memberId());

			var authorities = roles.stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
				.toList();

			CustomUserDetails userDetails = new CustomUserDetails(
				claims.memberId(),
				claims.email(),
				authorities
			);

			var authentication = new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				authorities
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(7);
		}
		return null;
	}

}

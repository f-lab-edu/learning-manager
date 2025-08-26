package me.chan99k.learningmanager.adapter.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import me.chan99k.learningmanager.common.exception.AuthenticateException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private FilterChain filterChain;

	private JwtAuthenticationFilter jwtAuthenticationFilter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	@DisplayName("[Success] 유효한 JWT 토큰이 있으면 인증 컨텍스트에 회원 ID를 설정하고 다음 필터로 진행한다")
	void doFilter_WithValidToken_SetsAuthenticationContext() throws IOException, ServletException {
		String validToken = "valid.jwt.token";
		String memberId = "12345";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/v1/members/profile/123");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenReturn(memberId);

		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		verify(jwtTokenProvider).validateToken(validToken);
		verify(jwtTokenProvider).getMemberIdFromToken(validToken);
		verify(filterChain).doFilter(request, response);

		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 보호된 경로에서 Authorization 헤더가 없으면 인증 예외가 발생한다")
	void doFilter_WithoutAuthorizationHeader_ThrowsAuthenticationException() throws IOException, ServletException {
		request.setRequestURI("/api/v1/members/profile/settings");

		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("Authorization 헤더가 없습니다");

		verify(jwtTokenProvider, never()).validateToken(any());
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] Bearer가 아닌 Authorization 헤더는 인증 예외가 발생한다")
	void doFilter_WithNonBearerAuthorizationHeader_ThrowsAuthenticationException()
		throws IOException, ServletException {
		request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
		request.setRequestURI("/api/v1/members/profile/123");

		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("Authorization 헤더 형식이 올바르지 않습니다");

		verify(jwtTokenProvider, never()).validateToken(any());
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 유효하지 않은 토큰이면 인증 예외가 발생한다")
	void doFilter_WithInvalidToken_ThrowsAuthenticationException() throws IOException, ServletException {
		String invalidToken = "invalid.jwt.token";
		request.addHeader("Authorization", "Bearer " + invalidToken);
		request.setRequestURI("/api/v1/members/profile/123");

		when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");

		verify(jwtTokenProvider).validateToken(invalidToken);
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 토큰 검증 중 AuthenticateException이 발생하면 예외가 발생한다")
	void doFilter_WhenTokenValidationThrowsAuthenticateException_RethrowsException()
		throws IOException, ServletException {
		String problematicToken = "problematic.jwt.token";
		request.addHeader("Authorization", "Bearer " + problematicToken);
		request.setRequestURI("/api/v1/member/profile");

		AuthenticateException originalException = new AuthenticateException(
			AuthProblemCode.FAILED_TO_VALIDATE_TOKEN
		);
		when(jwtTokenProvider.validateToken(problematicToken)).thenThrow(originalException);

		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰 유효성 검증에 실패하였습니다");

		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 회원 ID 추출 중 AuthenticateException이 발생하면 예외를 재포장하여 던진다")
	void doFilter_WhenGetMemberIdThrowsAuthenticateException_RethrowsException() throws IOException, ServletException {
		String validToken = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/v1/member/profile");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

		AuthenticateException originalException = new AuthenticateException(
			AuthProblemCode.INVALID_TOKEN_SUBJECT
		);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenThrow(originalException);

		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰의 subject 가 유효하지 않습니다");

		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 회원 ID를 Long으로 변환할 수 없으면 NumberFormatException을 AuthenticateException 으로 변환한 예외가 발생한다")
	void doFilter_WhenMemberIdIsNotNumeric_ThrowsAuthenticateException() throws IOException, ServletException {
		String validToken = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/v1/members/profile");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenReturn("not-a-number");

		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰의 subject 가 유효하지 않습니다");

		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Success] Bearer 토큰에 공백이 포함되어 있으면 그대로 처리한다")
	void doFilter_WithSpacesInBearerToken_ProcessesAsIs() throws IOException, ServletException {
		String validTokenWithSpaces = "valid.jwt.token.with.spaces";
		request.addHeader("Authorization", "Bearer " + validTokenWithSpaces);
		request.setRequestURI("/api/v1/member/profile");

		when(jwtTokenProvider.validateToken(validTokenWithSpaces)).thenReturn(true);
		when(jwtTokenProvider.getMemberIdFromToken(validTokenWithSpaces)).thenReturn("12345");

		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		verify(jwtTokenProvider).validateToken(validTokenWithSpaces);
		verify(jwtTokenProvider).getMemberIdFromToken(validTokenWithSpaces);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("[Failure] 빈 Bearer 토큰은 인증 예외가 발생한다")
	void doFilter_WithEmptyBearerToken_ThrowsAuthenticationException() throws IOException, ServletException {
		request.addHeader("Authorization", "Bearer ");
		request.setRequestURI("/api/v1/members/profile/123");

		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("Bearer 토큰이 비어있습니다");

		verify(jwtTokenProvider, never()).validateToken(any());
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Success] resolveToken 메서드가 올바르게 Bearer 토큰을 추출한다")
	void resolveToken_ExtractsBearerTokenCorrectly() throws Exception {
		String token = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + token);

		java.lang.reflect.Method resolveTokenMethod = JwtAuthenticationFilter.class
			.getDeclaredMethod("resolveToken", jakarta.servlet.http.HttpServletRequest.class);
		resolveTokenMethod.setAccessible(true);
		String extractedToken = (String)resolveTokenMethod.invoke(jwtAuthenticationFilter, request);

		assertThat(extractedToken).isEqualTo(token);
	}

	@Test
	@DisplayName("[Failure] resolveToken 메서드가 Authorization 헤더가 없으면 예외를 던진다")
	void resolveToken_WithoutAuthorizationHeader_ThrowsException() throws Exception {
		java.lang.reflect.Method resolveTokenMethod = JwtAuthenticationFilter.class
			.getDeclaredMethod("resolveToken", jakarta.servlet.http.HttpServletRequest.class);
		resolveTokenMethod.setAccessible(true);

		assertThatThrownBy(() -> resolveTokenMethod.invoke(jwtAuthenticationFilter, request))
			.hasCauseInstanceOf(AuthenticateException.class)
			.hasRootCauseMessage("[System] Authorization 헤더가 없습니다");
	}
}
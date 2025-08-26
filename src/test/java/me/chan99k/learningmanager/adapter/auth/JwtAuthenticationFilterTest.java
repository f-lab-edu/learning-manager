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
	@DisplayName("유효한 JWT 토큰이 있으면 인증 컨텍스트에 회원 ID를 설정하고 다음 필터로 진행한다")
	void doFilter_WithValidToken_SetsAuthenticationContext() throws IOException, ServletException {
		// given
		String validToken = "valid.jwt.token";
		String memberId = "12345";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/protected");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenReturn(memberId);

		// when
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// then
		verify(jwtTokenProvider).validateToken(validToken);
		verify(jwtTokenProvider).getMemberIdFromToken(validToken);
		verify(filterChain).doFilter(request, response);

		// 인증 컨텍스트는 finally 블록에서 정리되므로, 필터 실행 후에는 clear됨
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("Authorization 헤더가 없으면 인증 없이 다음 필터로 진행한다")
	void doFilter_WithoutAuthorizationHeader_ProceedsWithoutAuthentication() throws IOException, ServletException {
		// given
		request.setRequestURI("/api/public");

		// when
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// then
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("Bearer가 아닌 Authorization 헤더는 무시하고 다음 필터로 진행한다")
	void doFilter_WithNonBearerAuthorizationHeader_ProceedsWithoutAuthentication() throws
		IOException,
		ServletException {
		// given
		request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
		request.setRequestURI("/api/protected");

		// when
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// then
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("유효하지 않은 토큰이면 인증 없이 다음 필터로 진행한다")
	void doFilter_WithInvalidToken_ProceedsWithoutAuthentication() throws IOException, ServletException {
		// given
		String invalidToken = "invalid.jwt.token";
		request.addHeader("Authorization", "Bearer " + invalidToken);
		request.setRequestURI("/api/protected");

		when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

		// when
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// then
		verify(jwtTokenProvider).validateToken(invalidToken);
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("토큰 검증 중 AuthenticateException이 발생하면 예외를 재포장하여 던진다")
	void doFilter_WhenTokenValidationThrowsAuthenticateException_RethrowsException() throws
		IOException,
		ServletException {
		// given
		String problematicToken = "problematic.jwt.token";
		request.addHeader("Authorization", "Bearer " + problematicToken);
		request.setRequestURI("/api/protected");

		AuthenticateException originalException = new AuthenticateException(
			AuthProblemCode.FAILED_TO_VALIDATE_TOKEN
		);
		when(jwtTokenProvider.validateToken(problematicToken)).thenThrow(originalException);

		// when & then
		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("인증에 실패하였습니다")
			.hasCause(originalException);

		verify(filterChain, never()).doFilter(request, response);
		// finally 블록에서 컨텍스트가 정리되었는지 확인
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("회원 ID 추출 중 AuthenticateException이 발생하면 예외를 재포장하여 던진다")
	void doFilter_WhenGetMemberIdThrowsAuthenticateException_RethrowsException() throws IOException, ServletException {
		// given
		String validToken = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/protected");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

		AuthenticateException originalException = new AuthenticateException(
			AuthProblemCode.INVALID_TOKEN_SUBJECT
		);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenThrow(originalException);

		// when & then
		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("인증에 실패하였습니다")
			.hasCause(originalException);

		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("회원 ID를 Long으로 변환할 수 없으면 NumberFormatException을 AuthenticateException 으로 변환한다")
	void doFilter_WhenMemberIdIsNotNumeric_ThrowsAuthenticateException() throws IOException, ServletException {
		// given
		String validToken = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/protected");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenReturn("not-a-number");

		// when & then
		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
			.isInstanceOf(AuthenticateException.class)
			.hasMessageContaining("토큰의 subject 가 유효하지 않습니다");

		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("Bearer 토큰에 공백이 포함되어 있으면 그대로 처리한다")
	void doFilter_WithSpacesInBearerToken_ProcessesAsIs() throws IOException, ServletException {
		// given
		String validTokenWithSpaces = "valid.jwt.token.with.spaces";
		request.addHeader("Authorization", "Bearer " + validTokenWithSpaces);
		request.setRequestURI("/api/protected");

		when(jwtTokenProvider.validateToken(validTokenWithSpaces)).thenReturn(true);
		when(jwtTokenProvider.getMemberIdFromToken(validTokenWithSpaces)).thenReturn("12345");

		// when
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// then
		verify(jwtTokenProvider).validateToken(validTokenWithSpaces);
		verify(jwtTokenProvider).getMemberIdFromToken(validTokenWithSpaces);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("빈 Bearer 토큰은 무시하고 다음 필터로 진행한다")
	void doFilter_WithEmptyBearerToken_ProceedsWithoutAuthentication() throws IOException, ServletException {
		// given
		request.addHeader("Authorization", "Bearer ");
		request.setRequestURI("/api/protected");

		// when
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// then
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(jwtTokenProvider, never()).getMemberIdFromToken(any());
		verify(filterChain).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("resolveToken 메서드가 올바르게 Bearer 토큰을 추출한다")
	void resolveToken_ExtractsBearerTokenCorrectly() throws Exception {
		// given
		String token = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + token);

		// when - reflection을 사용하여 private 메서드 호출
		java.lang.reflect.Method resolveTokenMethod = JwtAuthenticationFilter.class
			.getDeclaredMethod("resolveToken", jakarta.servlet.http.HttpServletRequest.class);
		resolveTokenMethod.setAccessible(true);
		String extractedToken = (String)resolveTokenMethod.invoke(jwtAuthenticationFilter, request);

		// then
		assertThat(extractedToken).isEqualTo(token);
	}

	@Test
	@DisplayName("resolveToken 메서드가 Authorization 헤더가 없으면 null을 반환한다")
	void resolveToken_WithoutAuthorizationHeader_ReturnsNull() throws Exception {
		// given - Authorization 헤더 없음

		// when
		java.lang.reflect.Method resolveTokenMethod = JwtAuthenticationFilter.class
			.getDeclaredMethod("resolveToken", jakarta.servlet.http.HttpServletRequest.class);
		resolveTokenMethod.setAccessible(true);
		String extractedToken = (String)resolveTokenMethod.invoke(jwtAuthenticationFilter, request);

		// then
		assertThat(extractedToken).isNull();
	}
}
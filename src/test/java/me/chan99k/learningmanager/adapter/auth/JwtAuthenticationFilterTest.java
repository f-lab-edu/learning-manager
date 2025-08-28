package me.chan99k.learningmanager.adapter.auth;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.BDDMockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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

	private JwtAuthenticationFilter filter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		filter = new JwtAuthenticationFilter(jwtTokenProvider);
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

		filter.doFilter(request, response, filterChain);

		verify(jwtTokenProvider).validateToken(validToken);
		verify(jwtTokenProvider).getMemberIdFromToken(validToken);
		verify(filterChain).doFilter(request, response);

		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 보호된 경로에서 Authorization 헤더가 없으면 인증 예외가 발생한다")
	void doFilter_WithoutAuthorizationHeader_ThrowsAuthenticationException() throws IOException, ServletException {
		// given - Authorization 헤더 없이 요청 설정
		request.setRequestURI("/api/v1/members/profile");

		filter.doFilter(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
		assertThat(response.getContentType()).isEqualTo("application/problem+json;charset=UTF-8");

		// JSON 응답 검증
		String responseContent = response.getContentAsString();
		assertThat(responseContent).contains("\"status\":401");
		assertThat(responseContent).contains("\"code\":\"DAL005\"");
		assertThat(responseContent).contains("Authorization 헤더가 없습니다");
		assertThat(responseContent).contains("\"type\":\"https://api.lm.com/errors/DAL005\"");

		// filterChain.doFilter()가 호출되지 않았는지 확인
		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	@DisplayName("[Failure] Bearer가 아닌 Authorization 헤더는 인증 예외가 발생한다")
	void doFilter_WithNonBearerAuthorizationHeader_ThrowsAuthenticationException()
		throws IOException, ServletException {
		request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");

		request.setRequestURI("/api/v1/members/profile");

		filter.doFilter(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).isEqualTo("application/problem+json;charset=UTF-8");

		String responseContent = response.getContentAsString();
		assertThat(responseContent).contains("\"code\":\"DAL006\"");
		assertThat(responseContent).contains("Authorization 헤더 형식이 올바르지 않습니다");

		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	@DisplayName("[Failure] 유효하지 않은 토큰이면 401 응답을 반환한다")
	void doFilter_WithInvalidToken_ThrowsAuthenticationException() throws IOException, ServletException {
		// given
		String invalidToken = "invalid.jwt.token";
		request.addHeader("Authorization", "Bearer " + invalidToken);
		request.setRequestURI("/api/v1/members/profile");

		given(jwtTokenProvider.validateToken(invalidToken))
			.willThrow(new AuthenticateException(AuthProblemCode.FAILED_TO_VALIDATE_TOKEN));

		// when
		filter.doFilter(request, response, filterChain);

		// then
		assertThat(response.getStatus()).isEqualTo(401);
		String responseContent = response.getContentAsString();
		assertThat(responseContent).contains("\"code\":\"DAL002\"");
		assertThat(responseContent).contains("토큰 유효성 검증에 실패하였습니다");

		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	@DisplayName("[Failure] 토큰 검증 중 AuthenticateException이 발생하면 401 응답을 반환한다")
	void doFilter_WhenTokenValidationThrowsAuthenticateException_Returns401Response()
		throws IOException, ServletException {
		String problematicToken = "problematic.jwt.token";
		request.addHeader("Authorization", "Bearer " + problematicToken);
		request.setRequestURI("/api/v1/member/profile");

		AuthenticateException originalException = new AuthenticateException(
			AuthProblemCode.FAILED_TO_VALIDATE_TOKEN
		);
		when(jwtTokenProvider.validateToken(problematicToken)).thenThrow(originalException);
		filter.doFilter(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).isEqualTo("application/problem+json;charset=UTF-8");

		String responseContent = response.getContentAsString();
		assertThat(responseContent).contains("\"code\":\"DAL002\"");
		assertThat(responseContent).contains("토큰 유효성 검증에 실패하였습니다");


		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 회원 ID 추출 중 AuthenticateException이 발생하면 401 응답을 반환한다")
	void doFilter_WhenGetMemberIdThrowsAuthenticateException_Returns401Response() throws IOException, ServletException {
		String validToken = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/v1/member/profile");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

		AuthenticateException originalException = new AuthenticateException(
			AuthProblemCode.INVALID_TOKEN_SUBJECT
		);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenThrow(originalException);

		filter.doFilter(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).isEqualTo("application/problem+json;charset=UTF-8");

		String responseContent = response.getContentAsString();
		assertThat(responseContent).contains("\"code\":\"DAL003\"");
		assertThat(responseContent).contains("토큰의 subject 가 유효하지 않습니다");

		verify(filterChain, never()).doFilter(request, response);
		assertThat(AuthenticationContextHolder.getCurrentMemberId()).isEmpty();
	}

	@Test
	@DisplayName("[Failure] 회원 ID를 Long으로 변환할 수 없으면 401 응답을 반환한다")
	void doFilter_WhenMemberIdIsNotNumeric_Returns401Response() throws IOException, ServletException {
		String validToken = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + validToken);
		request.setRequestURI("/api/v1/members/profile");

		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
		when(jwtTokenProvider.getMemberIdFromToken(validToken)).thenReturn("not-a-number");
    
		filter.doFilter(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).isEqualTo("application/problem+json;charset=UTF-8");

		String responseContent = response.getContentAsString();
		assertThat(responseContent).contains("\"code\":\"DAL003\"");
		assertThat(responseContent).contains("토큰의 subject 가 유효하지 않습니다");
		
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

		filter.doFilter(request, response, filterChain);

		verify(jwtTokenProvider).validateToken(validTokenWithSpaces);
		verify(jwtTokenProvider).getMemberIdFromToken(validTokenWithSpaces);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("[Failure] 빈 Bearer 토큰은 인증 예외가 발생한다")
	void doFilter_WithEmptyBearerToken_ThrowsAuthenticationException() throws IOException, ServletException {
		// given
		request.addHeader("Authorization", "Bearer ");
		request.setRequestURI("/api/v1/members/profile");

		// when
		filter.doFilter(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(401);
		String responseContent = response.getContentAsString();
		assertThat(responseContent).contains("\"code\":\"DAL007\"");
		assertThat(responseContent).contains("Bearer 토큰이 비어있습니다");

		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	@DisplayName("[Success] resolveToken 메서드가 올바르게 Bearer 토큰을 추출한다")
	void resolveToken_ExtractsBearerTokenCorrectly() throws Exception {
		String token = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + token);

		java.lang.reflect.Method resolveTokenMethod = JwtAuthenticationFilter.class
			.getDeclaredMethod("resolveToken", jakarta.servlet.http.HttpServletRequest.class);
		resolveTokenMethod.setAccessible(true);
		String extractedToken = (String)resolveTokenMethod.invoke(filter, request);

		assertThat(extractedToken).isEqualTo(token);
	}

	@Test
	@DisplayName("[Failure] resolveToken 메서드가 Authorization 헤더가 없으면 예외를 던진다")
	void resolveToken_WithoutAuthorizationHeader_ThrowsException() throws Exception {
		java.lang.reflect.Method resolveTokenMethod = JwtAuthenticationFilter.class
			.getDeclaredMethod("resolveToken", jakarta.servlet.http.HttpServletRequest.class);
		resolveTokenMethod.setAccessible(true);

		assertThatThrownBy(() -> resolveTokenMethod.invoke(filter, request))
			.hasCauseInstanceOf(AuthenticateException.class)
			.hasRootCauseMessage("[System] Authorization 헤더가 없습니다");
	}
}
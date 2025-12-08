package me.chan99k.learningmanager.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	private static final Long MEMBER_ID = 1L;
	private static final String EMAIL = "test@example.com";
	private static final List<String> ROLES = List.of("MEMBER");
	private static final String VALID_TOKEN = "valid-jwt-token";
	private static final String INVALID_TOKEN = "invalid-jwt-token";

	@Mock
	JwtProvider jwtProvider;

	JwtAuthenticationFilter jwtAuthenticationFilter;

	MockHttpServletRequest request;
	MockHttpServletResponse response;
	MockFilterChain filterChain;

	@BeforeEach
	void setUp() {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = new MockFilterChain();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private JwtProvider.Claims createValidClaims() {
		return new JwtProvider.Claims(
			MEMBER_ID,
			EMAIL,
			ROLES,
			Instant.now().plusSeconds(3600)
		);
	}

	@Nested
	@DisplayName("유효한 토큰")
	class ValidTokenTest {

		@Test
		@DisplayName("SecurityContext에 인증 정보를 설정한다")
		void sets_authentication_in_security_context() throws Exception {
			request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
			given(jwtProvider.isValid(VALID_TOKEN)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(VALID_TOKEN)).willReturn(createValidClaims());

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isNotNull();
			assertThat(authentication.isAuthenticated()).isTrue();
		}

		@Test
		@DisplayName("CustomUserDetails에 올바른 정보를 포함한다")
		void custom_user_details_contains_correct_info() throws Exception {
			request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
			given(jwtProvider.isValid(VALID_TOKEN)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(VALID_TOKEN)).willReturn(createValidClaims());

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

			assertThat(userDetails.getMemberId()).isEqualTo(MEMBER_ID);
			assertThat(userDetails.getEmail()).isEqualTo(EMAIL);
			assertThat(userDetails.getAuthorities())
				.extracting("authority")
				.containsExactly("ROLE_MEMBER");
		}

		@Test
		@DisplayName("필터 체인이 계속 실행된다")
		void filter_chain_continues() throws Exception {
			request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
			given(jwtProvider.isValid(VALID_TOKEN)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(VALID_TOKEN)).willReturn(createValidClaims());

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			assertThat(filterChain.getRequest()).isNotNull();
			assertThat(filterChain.getResponse()).isNotNull();
		}
	}

	@Nested
	@DisplayName("무효한 토큰")
	class InvalidTokenTest {

		@Test
		@DisplayName("인증 정보 없이 필터를 통과한다")
		void passes_through_without_authentication() throws Exception {
			request.addHeader("Authorization", "Bearer " + INVALID_TOKEN);
			given(jwtProvider.isValid(INVALID_TOKEN)).willReturn(false);

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isNull();
		}

		@Test
		@DisplayName("필터 체인이 계속 실행된다")
		void filter_chain_continues() throws Exception {
			request.addHeader("Authorization", "Bearer " + INVALID_TOKEN);
			given(jwtProvider.isValid(INVALID_TOKEN)).willReturn(false);

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			assertThat(filterChain.getRequest()).isNotNull();
		}
	}

	@Nested
	@DisplayName("토큰 없음")
	class NoTokenTest {

		@Test
		@DisplayName("Authorization 헤더가 없으면 인증 정보 없이 통과한다")
		void passes_through_without_authentication() throws Exception {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isNull();
		}

		@Test
		@DisplayName("필터 체인이 계속 실행된다")
		void filter_chain_continues() throws Exception {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			assertThat(filterChain.getRequest()).isNotNull();
		}
	}

	@Nested
	@DisplayName("잘못된 형식")
	class WrongFormatTest {

		@Test
		@DisplayName("Bearer 접두사가 없으면 무시한다")
		void ignores_token_without_bearer_prefix() throws Exception {
			request.addHeader("Authorization", VALID_TOKEN);

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isNull();
		}

		@Test
		@DisplayName("빈 Authorization 헤더는 무시한다")
		void ignores_empty_authorization_header() throws Exception {
			request.addHeader("Authorization", "");

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isNull();
		}

		@Test
		@DisplayName("Bearer만 있고 토큰이 없으면 무시한다")
		void ignores_bearer_only() throws Exception {
			request.addHeader("Authorization", "Bearer ");

			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication).isNull();
		}
	}
}

package me.chan99k.learningmanager.controller;

import java.util.List;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.security.CustomUserDetails;

/**
 * 컨트롤러 슬라이스 테스트 기본 클래스
 *
 * @AutoConfigureMockMvc(addFilters = false): Security Filter Chain 비활성화
 * - JwtAuthenticationFilter 등 필터가 실행되지 않음
 * - @PreAuthorize 등 Method Security는 유지됨
 * - 필터 테스트는 JwtAuthenticationFilterTest에서 별도로 수행
 */
@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseControllerTest {

	protected MockMvc mockMvc;

	protected ObjectMapper objectMapper;

	// === 인증/인가 공통 MockBean ===

	@MockBean
	protected JwtProvider jwtProvider;

	@MockBean
	protected SystemAuthorizationPort systemAuthorizationPort;

	protected BaseControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	protected CustomUserDetails createMockUser(Long memberId) {
		return createMockUser(memberId, "test@example.com", "ROLE_USER");
	}

	protected CustomUserDetails createMockUser(Long memberId, String email, String... roles) {
		List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
			.map(SimpleGrantedAuthority::new)
			.toList();
		return new CustomUserDetails(memberId, email, authorities);
	}

	protected CustomUserDetails createAdminUser(Long memberId) {
		return createMockUser(memberId, "admin@example.com", "ROLE_ADMIN");
	}
}

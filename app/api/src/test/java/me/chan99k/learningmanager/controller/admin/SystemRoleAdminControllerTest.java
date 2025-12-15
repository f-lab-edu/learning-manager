package me.chan99k.learningmanager.controller.admin;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.admin.GrantSystemRole;
import me.chan99k.learningmanager.admin.RetrieveSystemRole;
import me.chan99k.learningmanager.admin.RevokeSystemRole;
import me.chan99k.learningmanager.advice.GlobalExceptionHandler;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemRoleAdminController 테스트")
class SystemRoleAdminControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final Long PERFORMER_ID = 999L;

	@InjectMocks
	private SystemRoleAdminController controller;

	@Mock
	private GrantSystemRole grantSystemRole;

	@Mock
	private RevokeSystemRole revokeSystemRole;

	@Mock
	private RetrieveSystemRole retrieveSystemRole;

	@BeforeEach
	void setUp() {
		// CustomUserDetails를 주입할 ArgumentResolver
		HandlerMethodArgumentResolver mockUserResolver = new HandlerMethodArgumentResolver() {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return parameter.getParameterType().equals(CustomUserDetails.class);
			}

			@Override
			public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
				NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
				return new CustomUserDetails(
					PERFORMER_ID,
					"admin@test.com",
					Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
				);
			}

		};

		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setCustomArgumentResolvers(mockUserResolver)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Nested
	@DisplayName("역할 부여 API (POST /api/v1/admin/members/{memberId}/roles)")
	class GrantRoleTest {

		@Test
		@DisplayName("[Success] 역할 부여 성공 - 201 CREATED")
		void grant_role_success() throws Exception {
			Long memberId = 1L;
			String requestJson = "{\"role\": \"OPERATOR\"}";

			doNothing().when(grantSystemRole).grant(any(GrantSystemRole.Request.class));

			mockMvc.perform(post("/api/v1/admin/members/{memberId}/roles", memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isCreated());

			then(grantSystemRole).should().grant(argThat(req ->
				req.memberId().equals(memberId)
					&& req.role() == SystemRole.OPERATOR
					&& req.performedBy().equals(PERFORMER_ID)
			));
		}
	}

	@Nested
	@DisplayName("역할 해제 API (DELETE /api/v1/admin/members/{memberId}/roles/{role})")
	class RevokeRoleTest {

		@Test
		@DisplayName("[Success] 역할 해제 성공 - 204 No Content")
		void revoke_role_success() throws Exception {
			Long memberId = 1L;

			doNothing().when(revokeSystemRole).revoke(any(RevokeSystemRole.Request.class));

			mockMvc.perform(delete("/api/v1/admin/members/{memberId}/roles/{role}", memberId, "OPERATOR"))
				.andDo(print())
				.andExpect(status().isNoContent());

			then(revokeSystemRole).should().revoke(argThat(req ->
				req.memberId().equals(memberId)
					&& req.role() == SystemRole.OPERATOR
					&& req.performedBy().equals(PERFORMER_ID)
			));
		}
	}

	@Nested
	@DisplayName("역할 조회 API (GET /api/v1/admin/members/{memberId}/roles)")
	class GetRolesTest {

		@Test
		@DisplayName("[Success] 역할 조회 성공 - 200 OK")
		void get_roles_success() throws Exception {
			Long memberId = 1L;
			Set<SystemRole> roles = Set.of(SystemRole.OPERATOR, SystemRole.AUDITOR);
			RetrieveSystemRole.Response response = new RetrieveSystemRole.Response(memberId, roles);

			given(retrieveSystemRole.retrieve(memberId)).willReturn(response);

			mockMvc.perform(get("/api/v1/admin/members/{memberId}/roles", memberId))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberId").value(memberId))
				.andExpect(jsonPath("$.roles").isArray());
		}
	}
}

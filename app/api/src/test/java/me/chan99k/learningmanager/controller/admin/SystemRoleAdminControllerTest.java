package me.chan99k.learningmanager.controller.admin;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.admin.GrantSystemRole;
import me.chan99k.learningmanager.admin.RetrieveSystemRole;
import me.chan99k.learningmanager.admin.RevokeSystemRole;
import me.chan99k.learningmanager.advice.GlobalExceptionHandler;
import me.chan99k.learningmanager.controller.BaseControllerTest;
import me.chan99k.learningmanager.member.SystemRole;

@WebMvcTest(controllers = SystemRoleAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("SystemRoleAdminController 테스트")
class SystemRoleAdminControllerTest extends BaseControllerTest {

	private static final Long ADMIN_MEMBER_ID = 999L;
	private static final Long TARGET_MEMBER_ID = 1L;

	@MockBean
	private GrantSystemRole grantSystemRole;

	@MockBean
	private RevokeSystemRole revokeSystemRole;

	@MockBean
	private RetrieveSystemRole retrieveSystemRole;

	@Autowired
	SystemRoleAdminControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		super(mockMvc, objectMapper);
	}

	// Note: 역할 부여/회수 API는 @AuthenticationPrincipal을 사용하므로
	// 필터가 비활성화된 상태에서는 테스트가 불가능합니다.
	// 해당 테스트는 SecurityIntegrationTest에서 별도로 수행합니다.

	@Nested
	@DisplayName("역할 조회 API (GET /api/v1/admin/members/{memberId}/roles)")
	class GetRolesTest {

		@Test
		@DisplayName("[Success] 역할 조회 성공 - 200 OK")
		void get_roles_success() throws Exception {
			Set<SystemRole> roles = Set.of(SystemRole.OPERATOR, SystemRole.AUDITOR);
			RetrieveSystemRole.Response response = new RetrieveSystemRole.Response(TARGET_MEMBER_ID, roles);
			given(retrieveSystemRole.retrieve(TARGET_MEMBER_ID)).willReturn(response);

			mockMvc.perform(get("/api/v1/admin/members/{memberId}/roles", TARGET_MEMBER_ID)
					.with(user(createAdminUser(ADMIN_MEMBER_ID))))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberId").value(TARGET_MEMBER_ID))
				.andExpect(jsonPath("$.roles").isArray())
				.andExpect(jsonPath("$.roles.length()").value(2));

			then(retrieveSystemRole).should().retrieve(TARGET_MEMBER_ID);
		}

		// Note: 보안 필터가 비활성화된 상태에서 권한/인증 테스트는 통합 테스트에서 수행
	}
}

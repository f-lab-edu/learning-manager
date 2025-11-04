package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.application.member.provides.MemberStatusChange;
import me.chan99k.learningmanager.domain.member.MemberStatus;

@WebMvcTest(controllers = MemberAdminController.class,
	excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
class MemberAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private MemberStatusChange memberStatusChange;

	@Test
	@DisplayName("[Success] 관리자가 회원 상태 변경 요청이 성공하면 204 No Content를 반환한다")
	void changeStatus_Success() throws Exception {
		Long memberId = 1L;
		MemberAdminController.ChangeStatusRequest request =
			new MemberAdminController.ChangeStatusRequest(MemberStatus.BANNED);

		doNothing().when(memberStatusChange).changeStatus(any(MemberStatusChange.Request.class));

		mockMvc.perform(put("/api/v1/admin/members/{memberId}/status", memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isNoContent());

		verify(memberStatusChange).changeStatus(argThat(req ->
			req.memberId().equals(memberId) &&
				req.status().equals(MemberStatus.BANNED)));
	}

	@Test
	@DisplayName("[Success] 회원 상태를 ACTIVE로 변경 요청이 성공한다")
	void changeStatus_Success_Activate() throws Exception {
		Long memberId = 2L;
		MemberAdminController.ChangeStatusRequest request =
			new MemberAdminController.ChangeStatusRequest(MemberStatus.ACTIVE);

		doNothing().when(memberStatusChange).changeStatus(any(MemberStatusChange.Request.class));

		mockMvc.perform(put("/api/v1/admin/members/{memberId}/status", memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isNoContent());

		verify(memberStatusChange).changeStatus(any(MemberStatusChange.Request.class));
	}
}
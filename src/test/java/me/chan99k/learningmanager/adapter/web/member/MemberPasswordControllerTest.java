package me.chan99k.learningmanager.adapter.web.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.adapter.auth.JwtTokenProvider;
import me.chan99k.learningmanager.application.member.provides.AccountPasswordChange;

@WebMvcTest(controllers = MemberPasswordController.class)
class MemberPasswordControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	@MockBean
	private AccountPasswordChange passwordChangeService;

	@Test
	@DisplayName("[Success] 유효한 요청으로 비밀번호 변경에 성공한다.")
	void test01() throws Exception {
		var request = new AccountPasswordChange.Request("test@example.com", "NewSecurePass456@");

		given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
		given(jwtTokenProvider.getMemberIdFromToken(anyString())).willReturn(String.valueOf(1L));

		given(passwordChangeService.changePassword(any(AccountPasswordChange.Request.class)))
			.willReturn(new AccountPasswordChange.Response());

		mockMvc.perform(put("/api/v1/members/password")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer mock-token")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

}
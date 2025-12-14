package me.chan99k.learningmanager.controller.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.PasswordChange;
import me.chan99k.learningmanager.security.CustomUserDetails;

// NOTE :: 단위 테스트로 변경
@WebMvcTest(controllers = PasswordChangeController.class)
@DisplayName("PasswordChangeController 테스트")
class PasswordChangeControllerTest {

	private static final Long MEMBER_ID = 1L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PasswordChange passwordChange;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private SystemAuthorizationPort systemAuthorizationPort;

	@Test
	@DisplayName("[Success] 비밀번호 변경 성공")
	void changePassword_success() throws Exception {
		doNothing().when(passwordChange).changePassword(anyLong(), any(PasswordChange.Request.class));

		mockMvc.perform(put("/api/v1/members/password")
				.with(user(createMockUser()))   // ← 인증 사용자 추가
				.with(csrf())                    // ← CSRF 토큰 추가
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					    "currentPassword": "OldPassword123!",
					    "newPassword": "NewPassword456!"
					}
					"""))
			.andDo(print())                      // ← 디버깅용 출력 추가
			.andExpect(status().isOk());
	}

	private CustomUserDetails createMockUser() {
		return new CustomUserDetails(
			MEMBER_ID,
			"user@example.com",
			List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
		);
	}

	@Nested
	class DomainExceptionCase {

		@Test
		@DisplayName("[Failure] 현재 비밀번호 불일치 - 400 Bad Request (INVALID_CREDENTIAL)")
		void changePassword_invalidCurrentPassword() throws Exception {
			doThrow(new DomainException(MemberProblemCode.INVALID_CREDENTIAL))
				.when(passwordChange).changePassword(anyLong(), any(PasswordChange.Request.class));

			mockMvc.perform(put("/api/v1/members/password")
					.with(user(createMockUser()))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						    "currentPassword": "WrongPassword123!",
						    "newPassword": "NewPassword456!"
						}
						"""))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("DML028"));
		}

		@Test
		@DisplayName("[Failure] 새 비밀번호가 현재와 동일 - 400 Bad Request (NEW_PASSWORD_SAME_AS_CURRENT)")
		void changePassword_sameAsCurrentPassword() throws Exception {
			doThrow(new DomainException(MemberProblemCode.NEW_PASSWORD_SAME_AS_CURRENT))
				.when(passwordChange).changePassword(anyLong(), any(PasswordChange.Request.class));

			mockMvc.perform(put("/api/v1/members/password")
					.with(user(createMockUser()))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						    "currentPassword": "SamePassword123!",
						    "newPassword": "SamePassword123!"
						}
						"""))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("DML029"));
		}

		@Test
		@DisplayName("[Failure] 회원 없음 - 400 Bad Request (MEMBER_NOT_FOUND)")
		void changePassword_memberNotFound() throws Exception {
			doThrow(new DomainException(MemberProblemCode.MEMBER_NOT_FOUND))
				.when(passwordChange).changePassword(anyLong(), any(PasswordChange.Request.class));

			mockMvc.perform(put("/api/v1/members/password")
					.with(user(createMockUser()))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						    "currentPassword": "OldPassword123!",
						    "newPassword": "NewPassword456!"
						}
						"""))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Domain Error"))
				.andExpect(jsonPath("$.code").value("DML026"));
		}
	}

	@Nested
	class ValidationExceptionCase {

		@Test
		@DisplayName("[Failure] 현재 비밀번호 누락 - 400 Bad Request (Validation Error)")
		void changePassword_missingCurrentPassword() throws Exception {
			mockMvc.perform(put("/api/v1/members/password")
					.with(user(createMockUser()))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						    "newPassword": "NewPassword456!"
						}
						"""))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 새 비밀번호 누락 - 400 Bad Request (Validation Error)")
		void changePassword_missingNewPassword() throws Exception {
			mockMvc.perform(put("/api/v1/members/password")
					.with(user(createMockUser()))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						    "currentPassword": "OldPassword123!"
						}
						"""))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 현재 비밀번호 빈값 - 400 Bad Request (Validation Error)")
		void changePassword_emptyCurrentPassword() throws Exception {
			mockMvc.perform(put("/api/v1/members/password")
					.with(user(createMockUser()))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						    "currentPassword": "",
						    "newPassword": "NewPassword456!"
						}
						"""))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}

		@Test
		@DisplayName("[Failure] 새 비밀번호 빈값 - 400 Bad Request (Validation Error)")
		void changePassword_emptyNewPassword() throws Exception {
			mockMvc.perform(put("/api/v1/members/password")
					.with(user(createMockUser()))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						    "currentPassword": "OldPassword123!",
						    "newPassword": ""
						}
						"""))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json;charset=UTF-8"))
				.andExpect(jsonPath("$.title").value("Validation Error"));
		}
	}
}

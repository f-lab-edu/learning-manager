package me.chan99k.learningmanager.controller.course;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.chan99k.learningmanager.authentication.JwtProvider;
import me.chan99k.learningmanager.authorization.CourseSecurity;
import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.config.SecurityConfig;
import me.chan99k.learningmanager.course.CourseMemberAddition;
import me.chan99k.learningmanager.course.CourseMemberRemoval;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.filter.JwtAuthenticationFilter;
import me.chan99k.learningmanager.member.SystemRole;

@WebMvcTest(controllers = CourseMemberController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("CourseMemberController 테스트")
class CourseMemberControllerTest {

	private static final Long MANAGER_ID = 1L;
	private static final Long NON_MANAGER_ID = 2L;
	private static final Long TARGET_MEMBER_ID = 100L;
	private static final Long COURSE_ID = 10L;
	private static final String MANAGER_EMAIL = "manager@example.com";
	private static final String VALID_TOKEN = "valid-jwt-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CourseMemberAddition courseMemberAddition;

	@MockBean
	private CourseMemberRemoval courseMemberRemoval;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private SystemAuthorizationPort systemAuthorizationPort;

	@MockBean(name = "courseSecurity")
	private CourseSecurity courseSecurity;

	@BeforeEach
	void setUp() {
		// 기본 인증 설정: MANAGER_ID로 인증된 사용자
		JwtProvider.Claims claims = new JwtProvider.Claims(
			MANAGER_ID,
			MANAGER_EMAIL,
			Instant.now().plusSeconds(3600)
		);

		given(jwtProvider.isValid(VALID_TOKEN)).willReturn(true);
		given(jwtProvider.validateAndGetClaims(VALID_TOKEN)).willReturn(claims);
		given(systemAuthorizationPort.getRoles(MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));

		// 기본: MANAGER_ID는 해당 과정의 매니저
		given(courseSecurity.isManager(COURSE_ID, MANAGER_ID)).willReturn(true);
	}

	@Nested
	@DisplayName("멤버 추가 API (POST /api/v1/courses/{courseId}/members)")
	class AddMemberTest {

		static Stream<Arguments> invalidRequestProvider() {
			return Stream.of(
				Arguments.of("빈 멤버 리스트", "{\"members\":[]}"),
				Arguments.of("잘못된 이메일 형식", "{\"members\":[{\"email\":\"invalid-email\",\"role\":\"MENTEE\"}]}"),
				Arguments.of("역할 누락", "{\"members\":[{\"email\":\"user@example.com\"}]}"),
				Arguments.of("이메일 누락", "{\"members\":[{\"role\":\"MENTEE\"}]}")
			);
		}

		@Test
		@DisplayName("[Success] 단일 멤버 추가 성공 - 200 OK")
		void add_single_member_success() throws Exception {
			List<CourseMemberAddition.MemberAdditionItem> members = List.of(
				new CourseMemberAddition.MemberAdditionItem("user@example.com", CourseRole.MENTEE)
			);
			CourseMemberAddition.Request request = new CourseMemberAddition.Request(members);

			doNothing().when(courseMemberAddition).addSingleMember(
				eq(MANAGER_ID), eq(COURSE_ID), any(CourseMemberAddition.MemberAdditionItem.class)
			);

			mockMvc.perform(post("/api/v1/courses/{courseId}/members", COURSE_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalCount").value(1))
				.andExpect(jsonPath("$.successCount").value(1))
				.andExpect(jsonPath("$.failureCount").value(0));

			then(courseMemberAddition).should().addSingleMember(
				eq(MANAGER_ID), eq(COURSE_ID), any(CourseMemberAddition.MemberAdditionItem.class)
			);
		}

		@Test
		@DisplayName("[Success] 다중 멤버 추가 성공 - 207 Multi-Status")
		void add_multiple_members_success() throws Exception {
			List<CourseMemberAddition.MemberAdditionItem> members = List.of(
				new CourseMemberAddition.MemberAdditionItem("user1@example.com", CourseRole.MENTEE),
				new CourseMemberAddition.MemberAdditionItem("user2@example.com", CourseRole.MENTOR)
			);
			CourseMemberAddition.Request request = new CourseMemberAddition.Request(members);

			CourseMemberAddition.Response response = new CourseMemberAddition.Response(
				2, 2, 0,
				List.of(
					new CourseMemberAddition.MemberResult("user1@example.com", CourseRole.MENTEE, "SUCCESS", "추가 성공"),
					new CourseMemberAddition.MemberResult("user2@example.com", CourseRole.MENTOR, "SUCCESS", "추가 성공")
				)
			);

			given(courseMemberAddition.addMultipleMembers(eq(MANAGER_ID), eq(COURSE_ID), anyList()))
				.willReturn(response);

			mockMvc.perform(post("/api/v1/courses/{courseId}/members", COURSE_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isMultiStatus())
				.andExpect(jsonPath("$.totalCount").value(2))
				.andExpect(jsonPath("$.successCount").value(2))
				.andExpect(jsonPath("$.failureCount").value(0))
				.andExpect(jsonPath("$.results.length()").value(2));

			then(courseMemberAddition).should().addMultipleMembers(eq(MANAGER_ID), eq(COURSE_ID), anyList());
		}

		@ParameterizedTest(name = "[Failure] {0} - 400 Bad Request")
		@MethodSource("invalidRequestProvider")
		@DisplayName("[Failure] 유효하지 않은 요청 - 400 Bad Request")
		void add_member_with_invalid_request(String testCase, String requestJson) throws Exception {
			mockMvc.perform(post("/api/v1/courses/{courseId}/members", COURSE_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

			then(courseMemberAddition).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void add_member_without_token() throws Exception {
			List<CourseMemberAddition.MemberAdditionItem> members = List.of(
				new CourseMemberAddition.MemberAdditionItem("user@example.com", CourseRole.MENTEE)
			);
			CourseMemberAddition.Request request = new CourseMemberAddition.Request(members);

			mockMvc.perform(post("/api/v1/courses/{courseId}/members", COURSE_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(courseMemberAddition).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 매니저 권한 없음 - 403 Forbidden")
		void add_member_without_manager_role() throws Exception {
			// NON_MANAGER_ID로 인증된 사용자 설정
			String nonManagerToken = "non-manager-token";
			JwtProvider.Claims nonManagerClaims = new JwtProvider.Claims(
				NON_MANAGER_ID,
				"user@example.com",
				Instant.now().plusSeconds(3600)
			);

			given(jwtProvider.isValid(nonManagerToken)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(nonManagerToken)).willReturn(nonManagerClaims);
			given(systemAuthorizationPort.getRoles(NON_MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));
			given(courseSecurity.isManager(COURSE_ID, NON_MANAGER_ID)).willReturn(false);

			List<CourseMemberAddition.MemberAdditionItem> members = List.of(
				new CourseMemberAddition.MemberAdditionItem("user@example.com", CourseRole.MENTEE)
			);
			CourseMemberAddition.Request request = new CourseMemberAddition.Request(members);

			mockMvc.perform(post("/api/v1/courses/{courseId}/members", COURSE_ID)
					.header("Authorization", "Bearer " + nonManagerToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(courseMemberAddition).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("멤버 제거 API (DELETE /api/v1/courses/{courseId}/members/{memberId})")
	class RemoveMemberTest {

		@Test
		@DisplayName("[Success] 멤버 제거 성공 - 204 No Content")
		void remove_member_success() throws Exception {
			doNothing().when(courseMemberRemoval)
				.removeMemberFromCourse(MANAGER_ID, COURSE_ID, TARGET_MEMBER_ID);

			mockMvc.perform(delete("/api/v1/courses/{courseId}/members/{memberId}",
					COURSE_ID, TARGET_MEMBER_ID)
					.header("Authorization", "Bearer " + VALID_TOKEN))
				.andDo(print())
				.andExpect(status().isNoContent());

			then(courseMemberRemoval).should()
				.removeMemberFromCourse(MANAGER_ID, COURSE_ID, TARGET_MEMBER_ID);
		}

		@Test
		@DisplayName("[Failure] 인증 토큰 없음 - 403 Forbidden")
		void remove_member_without_token() throws Exception {
			mockMvc.perform(delete("/api/v1/courses/{courseId}/members/{memberId}",
					COURSE_ID, TARGET_MEMBER_ID))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(courseMemberRemoval).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("[Failure] 매니저 권한 없음 - 403 Forbidden")
		void remove_member_without_manager_role() throws Exception {
			// NON_MANAGER_ID로 인증된 사용자 설정
			String nonManagerToken = "non-manager-token";
			JwtProvider.Claims nonManagerClaims = new JwtProvider.Claims(
				NON_MANAGER_ID,
				"user@example.com",
				Instant.now().plusSeconds(3600)
			);

			given(jwtProvider.isValid(nonManagerToken)).willReturn(true);
			given(jwtProvider.validateAndGetClaims(nonManagerToken)).willReturn(nonManagerClaims);
			given(systemAuthorizationPort.getRoles(NON_MANAGER_ID)).willReturn(Set.of(SystemRole.MEMBER));
			given(courseSecurity.isManager(COURSE_ID, NON_MANAGER_ID)).willReturn(false);

			mockMvc.perform(delete("/api/v1/courses/{courseId}/members/{memberId}",
					COURSE_ID, TARGET_MEMBER_ID)
					.header("Authorization", "Bearer " + nonManagerToken))
				.andDo(print())
				.andExpect(status().isForbidden());

			then(courseMemberRemoval).shouldHaveNoInteractions();
		}
	}
}

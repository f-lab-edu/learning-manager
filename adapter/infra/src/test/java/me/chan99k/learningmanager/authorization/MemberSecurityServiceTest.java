package me.chan99k.learningmanager.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.course.CourseRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberSecurityService 테스트")
class MemberSecurityServiceTest {

	private static final Long TARGET_MEMBER_ID = 1L;
	private static final Long CURRENT_MEMBER_ID = 1L;
	private static final Long OTHER_MEMBER_ID = 2L;
	private static final Long COURSE_ID = 10L;

	@InjectMocks
	private MemberSecurityService memberSecurityService;

	@Mock
	private CourseAuthorizationPort courseAuthorizationPort;

	// ========== isOwner / isNotOwner 공통 null 체크 ==========

	static Stream<Arguments> ownershipMethods() {
		return Stream.of(
			Arguments.of("isOwner",
				(BiFunction<MemberSecurityService, Long[], Boolean>)(svc, args) ->
					svc.isOwner(args[0], args[1])),
			Arguments.of("isNotOwner",
				(BiFunction<MemberSecurityService, Long[], Boolean>)(svc, args) ->
					svc.isNotOwner(args[0], args[1]))
		);
	}

	@ParameterizedTest(name = "{0}: targetMemberId가 null이면 isOwner=false를 기준으로 동작한다")
	@MethodSource("ownershipMethods")
	@DisplayName("[Edge] targetMemberId가 null이면 isOwner=false 기준으로 동작한다")
	void test01(String methodName, BiFunction<MemberSecurityService, Long[], Boolean> method) {
		boolean result = method.apply(memberSecurityService, new Long[] {null, CURRENT_MEMBER_ID});

		// isOwner -> false, isNotOwner -> true
		if (methodName.equals("isOwner")) {
			assertThat(result).isFalse();
		} else {
			assertThat(result).isTrue();
		}
	}

	@ParameterizedTest(name = "{0}: currentMemberId가 null이면 isOwner=false를 기준으로 동작한다")
	@MethodSource("ownershipMethods")
	@DisplayName("[Edge] currentMemberId가 null이면 isOwner=false 기준으로 동작한다")
	void test02(String methodName, BiFunction<MemberSecurityService, Long[], Boolean> method) {
		boolean result = method.apply(memberSecurityService, new Long[] {TARGET_MEMBER_ID, null});

		// isOwner -> false, isNotOwner -> true
		if (methodName.equals("isOwner")) {
			assertThat(result).isFalse();
		} else {
			assertThat(result).isTrue();
		}
	}

	// ========== isOwner 개별 테스트 ==========

	@Nested
	@DisplayName("isOwner")
	class IsOwnerTest {

		@Test
		@DisplayName("[Success] 동일한 ID이면 true를 반환한다")
		void test01() {
			boolean result = memberSecurityService.isOwner(TARGET_MEMBER_ID, CURRENT_MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 다른 ID이면 false를 반환한다")
		void test02() {
			boolean result = memberSecurityService.isOwner(TARGET_MEMBER_ID, OTHER_MEMBER_ID);

			assertThat(result).isFalse();
		}
	}

	// ========== isNotOwner 개별 테스트 ==========

	@Nested
	@DisplayName("isNotOwner")
	class IsNotOwnerTest {

		@Test
		@DisplayName("[Success] 다른 ID이면 true를 반환한다")
		void test01() {
			boolean result = memberSecurityService.isNotOwner(TARGET_MEMBER_ID, OTHER_MEMBER_ID);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 동일한 ID이면 false를 반환한다")
		void test02() {
			boolean result = memberSecurityService.isNotOwner(TARGET_MEMBER_ID, CURRENT_MEMBER_ID);

			assertThat(result).isFalse();
		}
	}

	// ========== isOwnerOrSystemAdmin 테스트 ==========

	@Nested
	@DisplayName("isOwnerOrSystemAdmin")
	class IsOwnerOrSystemAdminTest {

		@Test
		@DisplayName("[Success] 본인이면 true를 반환한다")
		void test01() {
			boolean result = memberSecurityService.isOwnerOrSystemAdmin(
				TARGET_MEMBER_ID, CURRENT_MEMBER_ID, false);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Success] 시스템 관리자이면 true를 반환한다")
		void test02() {
			boolean result = memberSecurityService.isOwnerOrSystemAdmin(
				TARGET_MEMBER_ID, OTHER_MEMBER_ID, true);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 본인도 아니고 관리자도 아니면 false를 반환한다")
		void test03() {
			boolean result = memberSecurityService.isOwnerOrSystemAdmin(
				TARGET_MEMBER_ID, OTHER_MEMBER_ID, false);

			assertThat(result).isFalse();
		}
	}

	// ========== isOwnerOrCourseManager 테스트 ==========

	@Nested
	@DisplayName("isOwnerOrCourseManager")
	class IsOwnerOrCourseManagerTest {

		@Test
		@DisplayName("[Success] 본인이면 true를 반환한다")
		void test01() {
			boolean result = memberSecurityService.isOwnerOrCourseManager(
				TARGET_MEMBER_ID, CURRENT_MEMBER_ID, COURSE_ID, courseAuthorizationPort);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Success] 과정 매니저이면 true를 반환한다")
		void test02() {
			when(courseAuthorizationPort.hasRole(OTHER_MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
				.thenReturn(true);

			boolean result = memberSecurityService.isOwnerOrCourseManager(
				TARGET_MEMBER_ID, OTHER_MEMBER_ID, COURSE_ID, courseAuthorizationPort);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("[Failure] 본인도 아니고 매니저도 아니면 false를 반환한다")
		void test03() {
			when(courseAuthorizationPort.hasRole(OTHER_MEMBER_ID, COURSE_ID, CourseRole.MANAGER))
				.thenReturn(false);

			boolean result = memberSecurityService.isOwnerOrCourseManager(
				TARGET_MEMBER_ID, OTHER_MEMBER_ID, COURSE_ID, courseAuthorizationPort);

			assertThat(result).isFalse();
		}
	}
}

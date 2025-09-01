package me.chan99k.learningmanager.application.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CourseCreation;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.SystemRole;

@ExtendWith(MockitoExtension.class)
class CourseCreationServiceTest {

	@Mock
	private CourseCommandRepository commandRepository;

	@Mock
	private MemberQueryRepository memberQueryRepository;

	@InjectMocks
	private CourseCreationService courseCreationService;

	@Test
	@DisplayName("[Success] 관리자 권한으로 과정 생성에 성공한다")
	void test01() {
		// given
		Long adminId = 1L;
		Member admin = mock(Member.class);
		when(admin.getRole()).thenReturn(SystemRole.ADMIN);
		when(memberQueryRepository.findById(adminId)).thenReturn(Optional.of(admin));

		Course mockCourse = mock(Course.class);
		when(mockCourse.getId()).thenReturn(100L);
		when(commandRepository.create(any(Course.class))).thenReturn(mockCourse);

		CourseCreation.Request request = new CourseCreation.Request("Test Course", "Test Description");

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(adminId));

			CourseCreation.Response response = courseCreationService.createCourse(request);

			assertThat(response.courseId()).isEqualTo(100L);
			verify(commandRepository).create(any(Course.class));
		}
	}

	@Test
	@DisplayName("[Failure] 일반 회원 권한으로는 과정 생성에 실패한다")
	void test02() {
		Long memberId = 1L;
		Member member = mock(Member.class);
		when(member.getRole()).thenReturn(SystemRole.MEMBER);
		when(memberQueryRepository.findById(memberId)).thenReturn(Optional.of(member));

		CourseCreation.Request request = new CourseCreation.Request("Test Course", "Test Description");

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(memberId));

			assertThatThrownBy(() -> courseCreationService.createCourse(request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("[Failure] 인증되지 않은 사용자는 과정 생성에 실패한다")
	void test03() {
		CourseCreation.Request request = new CourseCreation.Request("Test Course", "Test Description");

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.empty());

			assertThatThrownBy(() -> courseCreationService.createCourse(request))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 가입되지 않은 사용자는 과정 생성 시도에 실패한다.")
	void test04() {
		Long nonExistentMemberId = 999L;
		when(memberQueryRepository.findById(nonExistentMemberId)).thenReturn(Optional.empty());

		CourseCreation.Request request = new CourseCreation.Request("Test Course", "Test Description");

		try (MockedStatic<AuthenticationContextHolder> mockedContextHolder = mockStatic(
			AuthenticationContextHolder.class)) {
			mockedContextHolder.when(AuthenticationContextHolder::getCurrentMemberId)
				.thenReturn(Optional.of(nonExistentMemberId));

			assertThatThrownBy(() -> courseCreationService.createCourse(request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);
		}
	}
}
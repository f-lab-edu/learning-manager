package me.chan99k.learningmanager.application.course;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.application.course.provides.CourseMemberAddition;
import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthenticationException;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.common.exception.DomainException;
import me.chan99k.learningmanager.domain.course.Course;
import me.chan99k.learningmanager.domain.course.CourseMember;
import me.chan99k.learningmanager.domain.course.CourseProblemCode;
import me.chan99k.learningmanager.domain.course.CourseRole;
import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;

@ExtendWith(MockitoExtension.class)
class CourseMemberServiceTest {

	private final Long courseId = 1L;
	private final Long managerId = 10L;
	private final Long memberToAddId = 20L;
	private final String memberToAddEmail = "add@example.com";
	@InjectMocks
	private CourseMemberService courseMemberService;
	@Mock
	private CourseQueryRepository courseQueryRepository;
	@Mock
	private CourseCommandRepository courseCommandRepository;
	@Mock
	private MemberQueryRepository memberQueryRepository;
	@Mock
	private Course course;
	@Mock
	private Member manager;
	@Mock
	private Member memberToAdd;
	@Mock
	private CourseMember courseMember;

	@BeforeEach
	void setUp() {
		// 기본 Mock 객체 설정
		lenient().when(memberToAdd.getId()).thenReturn(memberToAddId);
	}

	private CourseMemberAddition.Request createRequest(String email, CourseRole role) {
		return new CourseMemberAddition.Request(email, role);
	}

	@Test
	@DisplayName("[Success] 과정 매니저가 새로운 멤버를 성공적으로 추가한다")
	void addMemberToCourse_Success() {
		// given
		CourseMemberAddition.Request request = createRequest(memberToAddEmail, CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findById(courseId)).thenReturn(Optional.of(course));
			when(memberQueryRepository.findByEmail(Email.of(memberToAddEmail))).thenReturn(Optional.of(memberToAdd));

			// isManager 권한 확인을 위한 설정
			when(course.getCourseMemberList()).thenReturn(List.of(courseMember));
			when(courseMember.getMemberId()).thenReturn(managerId);
			when(courseMember.getCourseRole()).thenReturn(CourseRole.MANAGER);

			// when
			courseMemberService.addMemberToCourse(courseId, request);

			// then
			verify(course).addMember(memberToAddId, CourseRole.MENTEE);
			verify(courseCommandRepository).save(course);
		}
	}

	@Test
	@DisplayName("[Failure] 인증된 사용자 정보가 없으면 AuthException이 발생한다")
	void addMemberToCourse_Fail_Unauthenticated() {
		// given
		CourseMemberAddition.Request request = createRequest(memberToAddEmail, CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> courseMemberService.addMemberToCourse(courseId, request))
				.isInstanceOf(AuthenticationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHENTICATION_CONTEXT_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 과정이 존재하지 않으면 DomainException이 발생한다")
	void addMemberToCourse_Fail_CourseNotFound() {
		// given
		CourseMemberAddition.Request request = createRequest(memberToAddEmail, CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findById(courseId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> courseMemberService.addMemberToCourse(courseId, request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", CourseProblemCode.COURSE_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 요청자가 과정의 매니저가 아니면 AuthException이 발생한다")
	void addMemberToCourse_Fail_NotManager() {
		// given
		CourseMemberAddition.Request request = createRequest(memberToAddEmail, CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findById(courseId)).thenReturn(Optional.of(course));

			// isManager 권한 확인을 위한 설정 (MENTEE 역할로 설정)
			when(course.getCourseMemberList()).thenReturn(List.of(courseMember));
			when(courseMember.getMemberId()).thenReturn(managerId);
			when(courseMember.getCourseRole()).thenReturn(CourseRole.MENTEE);

			// when & then
			assertThatThrownBy(() -> courseMemberService.addMemberToCourse(courseId, request))
				.isInstanceOf(AuthorizationException.class)
				.hasFieldOrPropertyWithValue("problemCode", AuthProblemCode.AUTHORIZATION_REQUIRED);
		}
	}

	@Test
	@DisplayName("[Failure] 추가하려는 멤버가 존재하지 않으면 DomainException이 발생한다")
	void addMemberToCourse_Fail_MemberToAddNotFound() {
		// given
		CourseMemberAddition.Request request = createRequest(memberToAddEmail, CourseRole.MENTEE);

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findById(courseId)).thenReturn(Optional.of(course));
			when(memberQueryRepository.findByEmail(Email.of(memberToAddEmail))).thenReturn(Optional.empty());

			// isManager 권한 확인
			when(course.getCourseMemberList()).thenReturn(List.of(courseMember));
			when(courseMember.getMemberId()).thenReturn(managerId);
			when(courseMember.getCourseRole()).thenReturn(CourseRole.MANAGER);

			// when & then
			assertThatThrownBy(() -> courseMemberService.addMemberToCourse(courseId, request))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("problemCode", MemberProblemCode.MEMBER_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("[Failure] 멤버를 추가하는 도중 도메인 규칙(이미 멤버가 존재)을 위반하면 예외가 발생한다")
	void addMemberToCourse_Fail_DomainRuleViolation() {
		// given
		CourseMemberAddition.Request request = createRequest(memberToAddEmail, CourseRole.MENTEE);
		doThrow(new IllegalArgumentException(CourseProblemCode.COURSE_MEMBER_ALREADY_REGISTERED.getMessage()))
			.when(course).addMember(anyLong(), any(CourseRole.class));

		try (MockedStatic<AuthenticationContextHolder> mockedContext = mockStatic(AuthenticationContextHolder.class)) {
			mockedContext.when(AuthenticationContextHolder::getCurrentMemberId).thenReturn(Optional.of(managerId));
			when(courseQueryRepository.findById(courseId)).thenReturn(Optional.of(course));
			when(memberQueryRepository.findByEmail(Email.of(memberToAddEmail))).thenReturn(Optional.of(memberToAdd));

			// isManager 권한 확인
			when(course.getCourseMemberList()).thenReturn(List.of(courseMember));
			when(courseMember.getMemberId()).thenReturn(managerId);
			when(courseMember.getCourseRole()).thenReturn(CourseRole.MANAGER);

			// when & then
			assertThatThrownBy(() -> courseMemberService.addMemberToCourse(courseId, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(CourseProblemCode.COURSE_MEMBER_ALREADY_REGISTERED.getMessage());

			verify(courseCommandRepository, never()).save(any(Course.class));
		}
	}
}

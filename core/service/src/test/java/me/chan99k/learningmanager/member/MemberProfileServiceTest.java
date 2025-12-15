package me.chan99k.learningmanager.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

	@Mock
	MemberQueryRepository memberQueryRepository;
	@Mock
	MemberCommandRepository memberCommandRepository;

	MemberProfileService memberProfileService;

	@BeforeEach
	void setUp() {
		memberProfileService = new MemberProfileService(memberQueryRepository, memberCommandRepository);
	}

	@Test
	@DisplayName("getProfile: 존재하는 회원이면 프로필을 반환한다")
	void getProfile_success() {
		TestMember m = new TestMember();
		m.updateProfile("img", "intro");
		given(memberQueryRepository.findById(1L)).willReturn(Optional.of(m));

		MemberProfileRetrieval.Response res = memberProfileService.getProfile(1L);

		assertEquals("img", res.profileImageUrl());
		assertEquals("intro", res.selfIntroduction());
	}

	@Test
	@DisplayName("getProfile: 회원이 없으면 MEMBER_NOT_FOUND를 던진다")
	void getProfile_notFound_throwsDomainException() {
		given(memberQueryRepository.findById(99L)).willReturn(Optional.empty());

		DomainException ex = assertThrows(DomainException.class, () -> memberProfileService.getProfile(99L));
		assertEquals(MemberProblemCode.MEMBER_NOT_FOUND, ex.getProblemCode());
	}

	@Test
	@DisplayName("getPublicProfile: 닉네임이 유효하지 않으면 IllegalArgumentException를 던진다")
		// TODO :: 커스텀 예외로 통합하기
	void getPublicProfile_invalidNickname_throwsIllegalArgument() {
		assertThrows(IllegalArgumentException.class, () -> memberProfileService.getPublicProfile(""));
	}

	@Test
	@DisplayName("updateProfile: 저장 후 memberId를 반환한다")
	void updateProfile_success_saves_and_returnsId() {
		TestMember m = new TestMember();
		given(memberQueryRepository.findById(1L)).willReturn(Optional.of(m));
		given(memberCommandRepository.save(m)).willReturn(m);

		MemberProfileUpdate.Response res = memberProfileService.updateProfile(1L,
			new MemberProfileUpdate.Request("img", "intro"));

		assertEquals(1L, res.memberId());
		then(memberCommandRepository).should().save(m);
	}

	static class TestMember extends Member {
		@Override
		public Long getId() {
			return 1L;
		}
	}
}

package me.chan99k.learningmanager.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

	@InjectMocks
	MemberProfileService memberProfileService;

	@Mock
	MemberQueryRepository memberQueryRepository;

	@Mock
	MemberCommandRepository memberCommandRepository;

	@Test
	@DisplayName("[Success] 존재하는 회원이면 프로필을 반환한다")
	void test01() {
		TestMember m = new TestMember();
		m.updateProfile("img", "intro");
		given(memberQueryRepository.findById(1L)).willReturn(Optional.of(m));

		MemberProfileRetrieval.Response res = memberProfileService.getProfile(1L);

		assertEquals("img", res.profileImageUrl());
		assertEquals("intro", res.selfIntroduction());
	}

	@Test
	@DisplayName("[Failure] 회원이 없으면 MEMBER_NOT_FOUND를 던진다")
	void test02() {
		given(memberQueryRepository.findById(99L)).willReturn(Optional.empty());

		DomainException ex = assertThrows(DomainException.class,
			() -> memberProfileService.getProfile(99L));

		assertEquals(MemberProblemCode.MEMBER_NOT_FOUND, ex.getProblemCode());
	}

	@Test
	@DisplayName("getPublicProfile: [Success] 존재하는 회원이면 프로필을 반환한다")
	void test03() {
		TestMember m = new TestMember();
		m.updateProfile("public-img", "public-intro");
		given(memberQueryRepository.findByNickName(any(Nickname.class))).willReturn(Optional.of(m));

		MemberProfileRetrieval.Response res = memberProfileService.getPublicProfile("validNickname");

		assertEquals("public-img", res.profileImageUrl());
		assertEquals("public-intro", res.selfIntroduction());
	}

	@Test
	@DisplayName("[Failure] 회원이 없으면 MEMBER_NOT_FOUND를 던진다")
	void test04() {
		given(memberQueryRepository.findByNickName(any(Nickname.class))).willReturn(Optional.empty());

		DomainException ex = assertThrows(DomainException.class,
			() -> memberProfileService.getPublicProfile("unknownNickname"));

		assertEquals(MemberProblemCode.MEMBER_NOT_FOUND, ex.getProblemCode());
	}

	@Test
	@DisplayName("[Failure] 닉네임이 유효하지 않으면 IllegalArgumentException를 던진다")
	void test05() {
		assertThrows(IllegalArgumentException.class,
			() -> memberProfileService.getPublicProfile(""));
	}

	@Test
	@DisplayName("[Success] 저장 후 memberId를 반환한다")
	void test06() {
		TestMember m = new TestMember();
		given(memberQueryRepository.findById(1L)).willReturn(Optional.of(m));
		given(memberCommandRepository.save(m)).willReturn(m);

		MemberProfileUpdate.Response res = memberProfileService.updateProfile(1L,
			new MemberProfileUpdate.Request("img", "intro"));

		assertEquals(1L, res.memberId());
		then(memberCommandRepository).should().save(m);
	}

	@Test
	@DisplayName("[Failure] 회원이 없으면 MEMBER_NOT_FOUND를 던진다")
	void test07() {
		given(memberQueryRepository.findById(99L)).willReturn(Optional.empty());
		MemberProfileUpdate.Request request = new MemberProfileUpdate.Request("img", "intro");

		DomainException ex = assertThrows(DomainException.class,
			() -> memberProfileService.updateProfile(99L, request));

		assertEquals(MemberProblemCode.MEMBER_NOT_FOUND, ex.getProblemCode());
	}

	static class TestMember extends Member {
		@Override
		public Long getId() {
			return 1L;
		}
	}
}

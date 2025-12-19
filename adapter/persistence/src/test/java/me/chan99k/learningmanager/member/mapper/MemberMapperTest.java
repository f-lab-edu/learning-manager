package me.chan99k.learningmanager.member.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.chan99k.learningmanager.member.Account;
import me.chan99k.learningmanager.member.AccountStatus;
import me.chan99k.learningmanager.member.Email;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberStatus;
import me.chan99k.learningmanager.member.Nickname;
import me.chan99k.learningmanager.member.entity.AccountEntity;
import me.chan99k.learningmanager.member.entity.MemberEntity;

@DisplayName("MemberMapper 테스트")
class MemberMapperTest {

	private static final Long MEMBER_ID = 1L;
	private static final Email PRIMARY_EMAIL = Email.of("test@example.com");
	private static final Nickname NICKNAME = Nickname.of("테스트닉네임");
	private static final MemberStatus STATUS = MemberStatus.ACTIVE;
	private static final String PROFILE_IMAGE_URL = "http://image.url";
	private static final String SELF_INTRODUCTION = "자기소개";
	private static final Instant NOW = Instant.now();
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;

	@Nested
	@DisplayName("toEntity")
	class ToEntityTest {

		@Test
		@DisplayName("[Success] 도메인 객체를 엔티티로 변환한다")
		void test01() {
			Account account = Account.reconstitute(
				1L, AccountStatus.ACTIVE, Email.of("test@example.com"), List.of(),
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);
			Member member = Member.reconstitute(
				MEMBER_ID, PRIMARY_EMAIL, NICKNAME, STATUS,
				PROFILE_IMAGE_URL, SELF_INTRODUCTION, List.of(account),
				NOW, CREATED_BY, NOW, CREATED_BY, VERSION
			);

			MemberEntity entity = MemberMapper.toEntity(member);

			assertThat(entity).isNotNull();
			assertThat(entity.getId()).isEqualTo(MEMBER_ID);
			assertThat(entity.getPrimaryEmail()).isEqualTo(PRIMARY_EMAIL);
			assertThat(entity.getNickname()).isEqualTo(NICKNAME.value());
			assertThat(entity.getStatus()).isEqualTo(STATUS);
			assertThat(entity.getProfileImageUrl()).isEqualTo(PROFILE_IMAGE_URL);
			assertThat(entity.getSelfIntroduction()).isEqualTo(SELF_INTRODUCTION);
			assertThat(entity.getAccounts()).hasSize(1);
		}

		@Test
		@DisplayName("[Edge] null 입력 시 null을 반환한다")
		void test02() {
			MemberEntity entity = MemberMapper.toEntity(null);

			assertThat(entity).isNull();
		}
	}

	@Nested
	@DisplayName("toDomain")
	class ToDomainTest {

		@Test
		@DisplayName("[Success] 엔티티를 도메인 객체로 변환한다")
		void test01() {
			MemberEntity entity = new MemberEntity();
			entity.setId(MEMBER_ID);
			entity.setPrimaryEmail(PRIMARY_EMAIL);
			entity.setNickname(NICKNAME.value());
			entity.setStatus(STATUS);
			entity.setProfileImageUrl(PROFILE_IMAGE_URL);
			entity.setSelfIntroduction(SELF_INTRODUCTION);
			entity.setCreatedAt(NOW);
			entity.setCreatedBy(CREATED_BY);
			entity.setLastModifiedAt(NOW);
			entity.setLastModifiedBy(CREATED_BY);
			entity.setVersion(VERSION);

			AccountEntity accountEntity = new AccountEntity();
			accountEntity.setId(1L);
			accountEntity.setMember(entity);
			accountEntity.setStatus(AccountStatus.ACTIVE);
			accountEntity.setEmail("test@example.com");
			accountEntity.setCreatedAt(NOW);
			accountEntity.setCreatedBy(CREATED_BY);
			accountEntity.setLastModifiedAt(NOW);
			accountEntity.setLastModifiedBy(CREATED_BY);
			accountEntity.setVersion(VERSION);
			entity.setAccounts(List.of(accountEntity));

			Member member = MemberMapper.toDomain(entity);

			assertThat(member).isNotNull();
			assertThat(member.getId()).isEqualTo(MEMBER_ID);
			assertThat(member.getPrimaryEmail()).isEqualTo(PRIMARY_EMAIL);
			assertThat(member.getNickname()).isEqualTo(NICKNAME);
			assertThat(member.getStatus()).isEqualTo(STATUS);
			assertThat(member.getProfileImageUrl()).isEqualTo(PROFILE_IMAGE_URL);
			assertThat(member.getSelfIntroduction()).isEqualTo(SELF_INTRODUCTION);
			assertThat(member.getAccounts()).hasSize(1);
		}

		@Test
		@DisplayName("[Edge] null 입력 시 null을 반환한다")
		void test02() {
			Member member = MemberMapper.toDomain(null);

			assertThat(member).isNull();
		}
	}
}

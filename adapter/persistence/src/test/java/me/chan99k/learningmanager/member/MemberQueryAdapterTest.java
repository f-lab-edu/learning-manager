package me.chan99k.learningmanager.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import me.chan99k.learningmanager.member.entity.MemberEntity;

@DisplayName("MemberQueryAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class MemberQueryAdapterTest {

	private static final Long MEMBER_ID = 1L;
	private static final Email PRIMARY_EMAIL = Email.of("test@example.com");
	private static final Nickname NICKNAME = Nickname.of("테스트닉네임");
	private static final MemberStatus STATUS = MemberStatus.ACTIVE;
	private static final Instant NOW = Instant.now();
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;
	@Mock
	private JpaMemberRepository jpaMemberRepository;
	private MemberQueryAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new MemberQueryAdapter(jpaMemberRepository);
	}

	@Test
	@DisplayName("[Success] findById로 회원을 조회한다")
	void test01() {
		MemberEntity entity = createTestMemberEntity();
		when(jpaMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(entity));

		Optional<Member> result = adapter.findById(MEMBER_ID);

		assertThat(result).isPresent();
		assertThat(result.get().getId()).isEqualTo(MEMBER_ID);
	}

	@Test
	@DisplayName("[Success] findById로 존재하지 않는 회원 조회 시 empty 반환")
	void test02() {
		when(jpaMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

		Optional<Member> result = adapter.findById(MEMBER_ID);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("[Success] findByEmail로 회원을 조회한다")
	void test03() {
		MemberEntity entity = createTestMemberEntity();
		when(jpaMemberRepository.findByAccountsEmail(PRIMARY_EMAIL.address()))
			.thenReturn(Optional.of(entity));

		Optional<Member> result = adapter.findByEmail(PRIMARY_EMAIL);

		assertThat(result).isPresent();
		assertThat(result.get().getPrimaryEmail()).isEqualTo(PRIMARY_EMAIL);
	}

	@Test
	@DisplayName("[Success] findByNickName로 회원을 조회한다")
	void test04() {
		MemberEntity entity = createTestMemberEntity();
		when(jpaMemberRepository.findByNickname(NICKNAME.value()))
			.thenReturn(Optional.of(entity));

		Optional<Member> result = adapter.findByNickName(NICKNAME);

		assertThat(result).isPresent();
		assertThat(result.get().getNickname()).isEqualTo(NICKNAME);
	}

	@Test
	@DisplayName("[Success] findMembersByEmails로 이메일로 회원 목록을 조회한다")
	void test05() {
		List<Email> emails = List.of(PRIMARY_EMAIL);
		Member member = createTestMember();
		MemberEmailPair pair = new MemberEmailPair(member, PRIMARY_EMAIL.address());
		when(jpaMemberRepository.findMemberEmailPairs(eq(emails), any(Limit.class)))
			.thenReturn(List.of(pair));

		List<MemberEmailPair> result = adapter.findMembersByEmails(emails, 10);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).member().getId()).isEqualTo(MEMBER_ID);
	}

	private Member createTestMember() {
		return Member.reconstitute(
			MEMBER_ID, PRIMARY_EMAIL, NICKNAME, STATUS,
			null, null, List.of(),
			NOW, CREATED_BY, NOW, CREATED_BY, VERSION
		);
	}

	private MemberEntity createTestMemberEntity() {
		MemberEntity entity = new MemberEntity();
		entity.setId(MEMBER_ID);
		entity.setPrimaryEmail(PRIMARY_EMAIL);
		entity.setNickname(NICKNAME.value());
		entity.setStatus(STATUS);
		entity.setCreatedAt(NOW);
		entity.setCreatedBy(CREATED_BY);
		entity.setLastModifiedAt(NOW);
		entity.setLastModifiedBy(CREATED_BY);
		entity.setVersion(VERSION);
		entity.setAccounts(List.of());
		return entity;
	}
}

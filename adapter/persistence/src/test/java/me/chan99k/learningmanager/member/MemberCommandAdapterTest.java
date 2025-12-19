package me.chan99k.learningmanager.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.member.entity.MemberEntity;

@DisplayName("MemberCommandAdapter 테스트")
@ExtendWith(MockitoExtension.class)
class MemberCommandAdapterTest {

	private static final Long MEMBER_ID = 1L;
	private static final Email PRIMARY_EMAIL = Email.of("test@example.com");
	private static final Nickname NICKNAME = Nickname.of("테스트닉네임");
	private static final MemberStatus STATUS = MemberStatus.ACTIVE;
	private static final Instant NOW = Instant.now();
	private static final Long CREATED_BY = 1L;
	private static final Long VERSION = 1L;
	@Mock
	private JpaMemberRepository jpaMemberRepository;
	private MemberCommandAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new MemberCommandAdapter(jpaMemberRepository);
	}

	@Test
	@DisplayName("[Success] save 메서드가 회원을 저장하고 도메인 객체를 반환한다")
	void test01() {
		Member member = createTestMember();
		MemberEntity savedEntity = createTestMemberEntity();
		when(jpaMemberRepository.save(any(MemberEntity.class))).thenReturn(savedEntity);

		Member result = adapter.save(member);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(MEMBER_ID);
		assertThat(result.getNickname()).isEqualTo(NICKNAME);
		verify(jpaMemberRepository).save(any(MemberEntity.class));
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

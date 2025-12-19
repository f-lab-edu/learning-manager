package me.chan99k.learningmanager.nickname;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RandomNicknameGenerator 테스트")
class RandomNicknameGeneratorTest {

	private RandomNicknameGenerator generator;

	@BeforeEach
	void setUp() {
		generator = new RandomNicknameGenerator();
	}

	@Test
	@DisplayName("[Success] 닉네임을 생성한다")
	void test01() {
		String nickname = generator.generate();

		assertThat(nickname).isNotNull();
		assertThat(nickname).isNotBlank();
	}

	@Test
	@DisplayName("[Success] 닉네임은 형용사 + 동물 + 숫자 형식이다")
	void test02() {
		// 형용사: 행복한, 똑똒한, 친절한, 나태한, 과감한, 수줍은
		// 동물: 강아지, 고양이, 앵무새, 돼지, 코요테, 코끼리, 호랑이
		// 숫자: 1 ~ 9999
		Pattern pattern = Pattern.compile(
			"^(행복한|똑똒한|친절한|나태한|과감한|수줍은)(강아지|고양이|앵무새|돼지|코요테|코끼리|호랑이)\\d+$"
		);

		String nickname = generator.generate();

		assertThat(nickname).matches(pattern);
	}

	@Test
	@DisplayName("[Success] 여러 번 호출 시 다른 닉네임을 생성할 수 있다")
	void test03() {
		Set<String> nicknames = new HashSet<>();

		// 10번 생성하여 최소 2개 이상의 서로 다른 닉네임이 나오는지 확인
		for (int i = 0; i < 10; i++) {
			nicknames.add(generator.generate());
		}

		// 랜덤 생성이므로 최소 2개 이상의 다른 닉네임이 생성되어야 함
		assertThat(nicknames.size()).isGreaterThanOrEqualTo(2);
	}
}

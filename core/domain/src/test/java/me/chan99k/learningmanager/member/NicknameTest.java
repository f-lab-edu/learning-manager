package me.chan99k.learningmanager.member;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Nickname 값 객체 테스트")
class NicknameTest {

	@Test
	@DisplayName("[Success] 닉네임 생성기를 통해 닉네임 자동 생성에 성공한다")
	void success_to_generate_nickname() {
		NicknameGenerator nicknameGenerator = () -> "generated";
		Nickname generatedNickname = Nickname.generateNickname(nicknameGenerator);
		assertThat(generatedNickname.value()).isEqualTo("generated");
	}

	@ParameterizedTest
	@DisplayName("[Failure] 제약조건에 맞지 않는 닉네임이라면 닉네임 생성에 실패한다.")
	@ValueSource(strings = {"", " ", "a", "특수문자!", "한글과특수문자!", "thisnicknameistoolong"})
	void fail_to_create_nickname(String invalidNicknameValue) {
		assertThatThrownBy(() -> new Nickname(invalidNicknameValue))
			.isInstanceOf(IllegalArgumentException.class);
	}
}

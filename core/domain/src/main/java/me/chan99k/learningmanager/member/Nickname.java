package me.chan99k.learningmanager.member;

import static me.chan99k.learningmanager.member.MemberProblemCode.*;

import java.util.regex.Pattern;

public record Nickname(String value) {
	private static final int MIN_LENGTH = 2;
	private static final int MAX_LENGTH = 17;
	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]*$");

	/**
	 * 사용자가 직접 닉네임을 지정하는 경우 사용하는 생성자.
	 * record의 컴팩트 생성자(compact constructor)를 사용하여 인스턴스 생성 전 유효성 검사를 수행합니다.
	 * @param value 사용자 입력 닉네임 문자열
	 */
	public Nickname {
		validate(value);
	}

	/**
	 * 처음 회원 가입시, 사용자 입력 없이 자동 생성 닉네임을 placseholder 마냥 지정해주는 용도로 사용하는 정적 팩터리
	 */
	public static Nickname generateNickname(NicknameGenerator nicknameGenerator) {
		return new Nickname(nicknameGenerator.generate());
	}

	/**
	 * 사용자 이름으로 닉네임을 생성하는 정적 팩터리 메서드
	 * @param input 사용자 입력 닉네임 문자열
	 * @return Nickname
	 */
	public static Nickname of(String input) {
		return new Nickname(input);
	}

	private void validate(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(MEMBER_NICKNAME_REQUIRED.getMessage());
		}
		if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
			throw new IllegalArgumentException(
				String.format(MEMBER_NICKNAME_CONSTRAINTS_FOR_LENGTH.getMessage(), MIN_LENGTH, MAX_LENGTH));
		}
		if (!NICKNAME_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException(MEMBER_NICKNAME_CONSTRAINTS_FOR_CHARACTER.getMessage());
		}
	}
}
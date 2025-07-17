package me.chan99k.learningmanager.domain.auth;

import static org.springframework.util.Assert.*;

import java.util.regex.Pattern;

public record Password(String encoded) {

	private static final int MIN_LENGTH = 8;
	private static final int MAX_LENGTH = 64;

	private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
	private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
	private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
	private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[^A-Za-z\\d].*");
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile(".*\\s.*");

	public static Password generatePassword(String password, PasswordEncoder encoder) {
		validatePattern(password);
		return new Password(encoder.encode(password));
	}

	public boolean verify(String password, PasswordEncoder encoder) {
		return encoder.match(password, this.encoded);
	}

	private static void validatePattern(String password) {
		state(password.length() >= MIN_LENGTH && password.length() <= MAX_LENGTH, "비밀번호는 최소 8자 이상, 64자 이하여야 합니다.");
		state(LOWERCASE_PATTERN.matcher(password).matches(), "비밀번호에는 소문자가 최소 1개 이상 포함되어야 합니다.");
		state(UPPERCASE_PATTERN.matcher(password).matches(), "비밀번호에는 대문자가 최소 1개 이상 포함되어야 합니다.");
		state(DIGIT_PATTERN.matcher(password).matches(), "비밀번호에는 숫자가 최소 1개 이상 포함되어야 합니다.");
		state(SPECIAL_CHAR_PATTERN.matcher(password).matches(), "비밀번호에는 특수문자가 최소 1개 이상 포함되어야 합니다.");
		state(!WHITESPACE_PATTERN.matcher(password).matches(), "비밀번호에 공백을 포함할 수 없습니다.");
	}

}

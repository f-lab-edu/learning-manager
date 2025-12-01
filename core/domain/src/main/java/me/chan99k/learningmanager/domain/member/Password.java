package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.springframework.util.Assert.*;

import java.util.regex.Pattern;

import jakarta.persistence.Embeddable;

@Embeddable
public record Password(String encoded) { // TODO :: 다중 인코더 알고리즘 시나리오에서는 알고리즘 이름을 가지고 있으면 좋을 수 있을 것 같다

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

	private static void validatePattern(String password) {
		state(password.length() >= MIN_LENGTH && password.length() <= MAX_LENGTH, PASSWORD_LENGTH_INVALID.getMessage());
		state(LOWERCASE_PATTERN.matcher(password).matches(), PASSWORD_NO_LOWERCASE.getMessage());
		state(UPPERCASE_PATTERN.matcher(password).matches(), PASSWORD_NO_UPPERCASE.getMessage());
		state(DIGIT_PATTERN.matcher(password).matches(), PASSWORD_NO_DIGIT.getMessage());
		state(SPECIAL_CHAR_PATTERN.matcher(password).matches(), PASSWORD_NO_SPECIAL_CHAR.getMessage());
		state(!WHITESPACE_PATTERN.matcher(password).matches(), PASSWORD_CONTAINS_WHITESPACE.getMessage());
	}

	public boolean matches(String plainPassword, PasswordEncoder encoder) {
		return encoder.matches(plainPassword, this.encoded);
	}

}


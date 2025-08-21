package me.chan99k.learningmanager.domain.member;

import static me.chan99k.learningmanager.domain.member.MemberProblemCode.*;
import static org.springframework.util.Assert.*;

import java.util.regex.Pattern;

import jakarta.persistence.Embeddable;

@Embeddable
public record Email(String address) {
	private static final Pattern PATTERN = Pattern.compile(
		"^(?!\\.)(?!.*\\.\\.)([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
	);

	public Email {
		isTrue(PATTERN.matcher(address).matches(), INVALID_EMAIL_FORMAT.getMessage());
	}

	public static Email of(String input) {
		return new Email(input);
	}
}

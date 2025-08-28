package me.chan99k.learningmanager.domain.member;

/**
 * 사용자 패스워드에 대하여 단방향 암호화 및 비교를 수행하는 인터페이스.
 */
public interface PasswordEncoder {
	String encode(String rawString);

	boolean matches(String rawString, String encoded);
}

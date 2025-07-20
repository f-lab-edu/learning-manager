package me.chan99k.learningmanager.domain.member;

/**
 * 사용자가 닉네임을 제공하지 않으면 특정한 규칙에 따라 자동으로 닉네임을 생성하는 인터페이스.
 */
public interface NicknameGenerator {
	String generate();
}

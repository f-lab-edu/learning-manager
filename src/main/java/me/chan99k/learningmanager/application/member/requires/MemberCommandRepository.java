package me.chan99k.learningmanager.application.member.requires;

import me.chan99k.learningmanager.domain.member.Member;

public interface MemberCommandRepository {
	Member save(Member member);
}

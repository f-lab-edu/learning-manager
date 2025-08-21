package me.chan99k.learningmanager.application.member.requires;

import java.util.Optional;

import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;

public interface MemberQueryRepository {
	Optional<Member> findById(Long memberId);

	Optional<Member> findByEmail(Email email);
}

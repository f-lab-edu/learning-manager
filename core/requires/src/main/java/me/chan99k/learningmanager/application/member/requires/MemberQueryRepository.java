package me.chan99k.learningmanager.application.member.requires;

import java.util.List;
import java.util.Optional;

import me.chan99k.learningmanager.domain.member.Email;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.Nickname;

public interface MemberQueryRepository {
	Optional<Member> findById(Long memberId);

	Optional<Member> findByEmail(Email email);

	Optional<Member> findByNickName(Nickname nickname);

	List<MemberEmailPair> findMembersByEmails(List<Email> emails, int limit);

}

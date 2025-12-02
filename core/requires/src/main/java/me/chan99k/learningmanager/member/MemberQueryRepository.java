package me.chan99k.learningmanager.member;

import java.util.List;
import java.util.Optional;

public interface MemberQueryRepository {
	Optional<Member> findById(Long memberId);

	Optional<Member> findByEmail(Email email);

	Optional<Member> findByNickName(Nickname nickname);

	List<MemberEmailPair> findMembersByEmails(List<Email> emails, int limit);

}

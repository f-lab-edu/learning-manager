package me.chan99k.learningmanager.application.member;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.application.member.provides.AccountAddition;
import me.chan99k.learningmanager.application.member.provides.MemberRegistration;
import me.chan99k.learningmanager.application.member.requires.MemberRepository;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.NicknameGenerator;
import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Service
public class MemberService implements MemberRegistration, AccountAddition {
	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;

	private final NicknameGenerator nicknameGenerator;

	public MemberService(
		MemberRepository memberRepository, PasswordEncoder passwordEncoder, NicknameGenerator nicknameGenerator
	) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.nicknameGenerator = nicknameGenerator;
	}

	@Override
	public MemberRegistration.Response register(MemberRegistration.Request request) {
		Member newMember = Member.registerDefault(nicknameGenerator);
		newMember.addAccount(request.email(), request.rawPassword(), passwordEncoder);

		Member saved = memberRepository.save(newMember);

		return new MemberRegistration.Response(saved.getId());
	}
}

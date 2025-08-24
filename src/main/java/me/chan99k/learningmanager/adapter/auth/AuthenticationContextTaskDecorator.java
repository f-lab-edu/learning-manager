package me.chan99k.learningmanager.adapter.auth;

import java.util.Optional;

import org.springframework.core.task.TaskDecorator;

/**
 * 현재 스레드의 인증 컨텍스트를 다른 스레드로 전파하는 태스크 데코레이터
 */
public class AuthenticationContextTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		Optional<Long> currentMemberId = AuthenticationContextHolder.getCurrentMemberId();

		return () -> {
			try {
				currentMemberId.ifPresent(AuthenticationContextHolder::setCurrentMemberId);
				runnable.run();
			} finally {
				AuthenticationContextHolder.clear(); // 인증 컨텍스트가 없으면 그냥 없이 진행
			}
		};
	}
}

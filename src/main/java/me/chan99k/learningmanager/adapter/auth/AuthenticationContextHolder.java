package me.chan99k.learningmanager.adapter.auth;

import java.util.Optional;

/**
 * "현재 로그인한 사용자" 에 대한 정보에 접근하기 위한 유틸리티 클래스.
 * <p>
 * - controller(memberId) -> service(memberId) -> repository(memberId) : 모든 메서드 시그니처 변경 필요
 * <p>
 * -  HTTP Session 사용 : Stateful
 * <p>
 * - HttpServletRequest.setAttribute() : Controller에서만 접근 가능하도록 하여야 계층간 의존성 원칙을 지킬 수 있음
 */
public class AuthenticationContextHolder { // TODO :: HTTP 요청당 하나의 MemberID 장단?
	/**
	 *  HTTP 요청 스레드에서 생성된 자식 스레드(CompletableFuture, @Async)에서도 인증 정보를 상속하기 위해 InheritableThreadLocal 사용
	 */
	private static final InheritableThreadLocal<Long> currentMemberId = new InheritableThreadLocal<>();

	public static Optional<Long> getCurrentMemberId() {
		return Optional.ofNullable(currentMemberId.get());
	}

	public static void setCurrentMemberId(Long memberId) {
		currentMemberId.set(memberId);
	}

	public static void clear() {
		currentMemberId.remove();
	}

	public static boolean isAuthenticated() {
		return currentMemberId.get() != null;
	}
}
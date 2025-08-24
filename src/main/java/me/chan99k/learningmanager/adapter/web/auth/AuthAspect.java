package me.chan99k.learningmanager.adapter.web.auth;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.common.exception.UnauthenticatedException;

@Aspect
@Component
public class AuthAspect {
	public static final Logger log = LoggerFactory.getLogger(AuthAspect.class);

	@Around("@within(RequireAuthentication) || @annotation(RequireAuthentication)")
	public Object checkAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
		if (!AuthenticationContextHolder.isAuthenticated()) {
			log.warn("Unauthorized access attempt to method: {}.{}",
				joinPoint.getTarget().getClass().getSimpleName(),
				joinPoint.getSignature().getName()
			);
			throw new UnauthenticatedException("[System] 인증이 필요한 요청입니다");
		}
		log.debug("Authentication check passed for user : {}",
			AuthenticationContextHolder.getCurrentMemberId().orElse(null));
		return joinPoint.proceed();
	}
}

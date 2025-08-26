package me.chan99k.learningmanager.adapter.web.auth;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.adapter.auth.AuthenticationContextHolder;
import me.chan99k.learningmanager.common.exception.AuthenticateException;

@Aspect
@Component
public class AuthenticationAspect {
	public static final Logger log = LoggerFactory.getLogger(AuthenticationAspect.class);

	@Around("@within(RequireAuthentication) || @annotation(RequireAuthentication)")
	public Object checkAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
		if (!AuthenticationContextHolder.isAuthenticated()) {
			log.warn("Unauthorized access attempt to method: {}.{}",
				joinPoint.getTarget().getClass().getSimpleName(),
				joinPoint.getSignature().getName()
			);
			throw new AuthenticateException(AuthProblemCode.AUTHENTICATION_REQUIRED);
		}
		log.debug("Authentication check passed for user : {}",
			AuthenticationContextHolder.getCurrentMemberId().orElse(null));
		return joinPoint.proceed();
	}
}

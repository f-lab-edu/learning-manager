package me.chan99k.learningmanager.adapter.web.attendance;

import static org.mockito.Mockito.*;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import me.chan99k.learningmanager.security.CustomUserDetails;

/**
 * Security가 비활성화된 WebMvcTest에서 @AuthenticationPrincipal CustomUserDetails를 주입하기 위한
 * 테스트용 ArgumentResolver.
 * <p>
 * Security 설정이 비활성화되어 있을 때 @AuthenticationPrincipal은 null을 주입하므로,
 * 이 Resolver를 사용하여 mock CustomUserDetails를 주입
 */
public class MockCustomUserDetailsArgumentResolver implements HandlerMethodArgumentResolver {

	private static final Long DEFAULT_MEMBER_ID = 123L;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
			&& CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		CustomUserDetails mockUser = mock(CustomUserDetails.class);
		when(mockUser.getMemberId()).thenReturn(DEFAULT_MEMBER_ID);
		return mockUser;
	}
}

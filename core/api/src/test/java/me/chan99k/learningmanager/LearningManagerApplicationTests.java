package me.chan99k.learningmanager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 전체 애플리케이션 컨텍스트 로드 테스트.
 * adapter 모듈 의존성이 필요하므로 별도의 통합 테스트 모듈로 이동 필요.
 */
@Disabled("통합 테스트 모듈로 이동 예정")
@SpringBootTest
@ActiveProfiles("test")
class LearningManagerApplicationTests {

	@Test
	void contextLoads() {
	}

}

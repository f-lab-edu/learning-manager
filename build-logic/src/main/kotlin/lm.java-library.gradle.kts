plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("lm.java-jacoco")
}

group = "me.chan99k"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    // maxParallelForks: 동시에 실행할 테스트 프로세스 수
    // Runtime.getRuntime().availableProcessors() / 2 -> CPU 코어의 절반 사용
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    // forkEvery: N개 테스트마다 새 JVM 프로세스 생성 (메모리 누수 방지)
    // 0 = 재시작 안 함 (기본값), 250 = 250개 테스트마다 재시작
    setForkEvery(250)
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "testImplementation"(catalog.findLibrary("spring-boot-starter-test").get())
    "testRuntimeOnly"(catalog.findLibrary("junit-platform-launcher").get())
}

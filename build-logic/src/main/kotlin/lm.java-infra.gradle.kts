plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(catalog.findLibrary("spring-context").get())
    "implementation"(catalog.findLibrary("spring-security-crypto").get())

    "implementation"(catalog.findLibrary("jjwt-api").get())
    "runtimeOnly"(catalog.findLibrary("jjwt-impl").get())
    "runtimeOnly"(catalog.findLibrary("jjwt-jackson").get())

    // jjwt-impl이 내부적으로 Jackson 어노테이션 사용 - 컴파일 경고 방지
    "compileOnly"(catalog.findLibrary("jackson-annotations").get())

    "implementation"(catalog.findLibrary("slf4j-api").get())
}
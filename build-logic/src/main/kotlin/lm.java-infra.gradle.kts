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

    "implementation"(catalog.findLibrary("slf4j-api").get())
}
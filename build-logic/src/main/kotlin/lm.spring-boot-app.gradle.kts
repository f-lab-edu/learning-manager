plugins {
    id("lm.java-library")
    id("org.springframework.boot")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(catalog.findLibrary("spring-boot-starter-web").get())
    "implementation"(catalog.findLibrary("spring-boot-starter-validation").get())
    "implementation"(catalog.findLibrary("spring-boot-starter-security").get())
    "implementation"(catalog.findLibrary("spring-boot-starter-oauth2-resource-server").get())
    "implementation"(catalog.findLibrary("spring-boot-starter-data-jpa").get())

    "testImplementation"(catalog.findLibrary("spring-security-test").get())
    "testImplementation"(catalog.findLibrary("spring-boot-testcontainers").get())
    "testImplementation"(catalog.findLibrary("testcontainers-junit").get())
    "testImplementation"(catalog.findLibrary("testcontainers-mysql").get())
    "testImplementation"(catalog.findLibrary("testcontainers-mongodb").get())
}

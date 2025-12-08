plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("lm.java-jacoco")
}

group = "me.chan99k"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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

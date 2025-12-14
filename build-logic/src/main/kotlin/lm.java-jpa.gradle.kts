plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "api"(catalog.findLibrary("spring-boot-starter-data-jpa").get())
    "api"(catalog.findLibrary("flyway-core").get())
    "api"(catalog.findLibrary("flyway-mysql").get())

    "runtimeOnly"(catalog.findLibrary("mysql-connector").get())
    "runtimeOnly"(catalog.findLibrary("h2").get())

    // QueryDSL
    "implementation"(variantOf(catalog.findLibrary("querydsl-jpa").get()) { classifier("jakarta") })
    "annotationProcessor"(variantOf(catalog.findLibrary("querydsl-apt").get()) { classifier("jakarta") })
    "annotationProcessor"("jakarta.annotation:jakarta.annotation-api")
    "annotationProcessor"("jakarta.persistence:jakarta.persistence-api")

    "testImplementation"(catalog.findLibrary("testcontainers-mysql").get())
    "testImplementation"(catalog.findLibrary("testcontainers-junit").get())
}

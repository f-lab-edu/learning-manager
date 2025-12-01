plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "api"(catalog.findLibrary("spring-boot-starter-data-mongodb").get())
    "api"(catalog.findLibrary("mongock-springboot").get())
    "api"(catalog.findLibrary("mongock-mongodb-driver").get())

    "testImplementation"(catalog.findLibrary("spring-boot-testcontainers").get())
    "testImplementation"(catalog.findLibrary("testcontainers-junit").get())
    "testImplementation"(catalog.findLibrary("testcontainers-mongodb").get())
}

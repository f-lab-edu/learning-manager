plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    api(catalog.findLibrary("spring-boot-starter-actuator").get())
    api(catalog.findLibrary("micrometer-prometheus").get())
}
plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // JPA for domain entities (they use JPA annotations)
    implementation(catalog.findLibrary("spring-boot-starter-data-jpa").get())
}
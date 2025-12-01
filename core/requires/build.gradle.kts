plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core:domain"))

    // For Page, Pageable
    implementation(catalog.findLibrary("spring-boot-starter-data-jpa").get())
}
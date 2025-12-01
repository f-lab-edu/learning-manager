plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))

    // For Page, Pageable
    implementation(catalog.findLibrary("spring-data-commons").get())

    // For Jakarta Validation annotations
    implementation(catalog.findLibrary("jakarta-validation-api").get())
}

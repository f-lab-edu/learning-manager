plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))

    implementation(catalog.findLibrary("spring-data-commons").get())

    implementation(catalog.findLibrary("jakarta-validation-api").get())
}

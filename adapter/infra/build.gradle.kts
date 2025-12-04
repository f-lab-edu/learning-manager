plugins {
    id("lm.java-infra")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:requires"))

    implementation(catalog.findLibrary("spring-tx").get())
    implementation(catalog.findLibrary("spring-context-support").get())
    implementation(catalog.findLibrary("angus-mail").get())
}

plugins {
    id("lm.spring-boot-app")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // Core modules
    implementation(project(":core:domain"))
    implementation(project(":core:provides"))
    implementation(project(":core:requires"))
    implementation(project(":core:service"))

    // JWT for auth adapters
    implementation(catalog.findLibrary("jjwt-api").get())
    runtimeOnly(catalog.findLibrary("jjwt-impl").get())
    runtimeOnly(catalog.findLibrary("jjwt-jackson").get())

    // Adapters - runtime only (DI)
    runtimeOnly(project(":adapter:mysql"))
    runtimeOnly(project(":adapter:mongo"))
}
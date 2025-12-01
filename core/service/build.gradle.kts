plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:provides"))
    implementation(project(":core:requires"))

    implementation(catalog.findLibrary("spring-context").get())

    implementation(catalog.findLibrary("spring-tx").get())

    implementation(catalog.findLibrary("spring-data-commons").get())

    implementation(catalog.findLibrary("jjwt-api").get())
    runtimeOnly(catalog.findLibrary("jjwt-impl").get())
    runtimeOnly(catalog.findLibrary("jjwt-jackson").get())

    implementation(catalog.findLibrary("jbcrypt").get())

    implementation(catalog.findLibrary("spring-boot-starter-security").get())
    implementation(catalog.findLibrary("spring-boot-starter-oauth2-resource-server").get())
}
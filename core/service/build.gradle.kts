plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:provides"))
    implementation(project(":core:requires"))

    // Spring Context for @Service, @Component
    implementation(catalog.findLibrary("spring-context").get())

    // Transaction management
    implementation(catalog.findLibrary("spring-tx").get())

    // Spring Data Commons for Page, Pageable
    implementation(catalog.findLibrary("spring-data-commons").get())

    // JWT
    implementation(catalog.findLibrary("jjwt-api").get())
    runtimeOnly(catalog.findLibrary("jjwt-impl").get())
    runtimeOnly(catalog.findLibrary("jjwt-jackson").get())

    // Password encoding
    implementation(catalog.findLibrary("jbcrypt").get())

    // Security (for OAuth2 JWT)
    implementation(catalog.findLibrary("spring-boot-starter-security").get())
    implementation(catalog.findLibrary("spring-boot-starter-oauth2-resource-server").get())
}
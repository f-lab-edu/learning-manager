plugins {
    id("lm.spring-boot-app")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:provides"))
    implementation(project(":core:requires"))
    implementation(project(":core:service"))

    implementation(catalog.findLibrary("jjwt-api").get())
    runtimeOnly(catalog.findLibrary("jjwt-impl").get())
    runtimeOnly(catalog.findLibrary("jjwt-jackson").get())

    runtimeOnly(project(":adapter:mysql"))
    runtimeOnly(project(":adapter:mongo"))
}
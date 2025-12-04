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
    implementation(catalog.findLibrary("spring-boot-starter-web").get())
    implementation(catalog.findLibrary("spring-dotenv").get())
    runtimeOnly(catalog.findLibrary("jjwt-impl").get())
    runtimeOnly(catalog.findLibrary("jjwt-jackson").get())

    runtimeOnly(project(":adapter:persistence"))
    runtimeOnly(project(":adapter:mongo"))
    runtimeOnly(project(":adapter:infra"))

    // Support 모듈
    runtimeOnly(project(":support:logging"))
    implementation(project(":support:monitoring"))
}
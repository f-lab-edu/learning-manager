plugins {
    id("lm.java-library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(catalog.findLibrary("spring-core").get())
    implementation(catalog.findLibrary("slf4j-api").get())
}